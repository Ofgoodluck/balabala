#!/usr/bin/env python3
import os
import hashlib
import json
from flask import Flask, request, jsonify, Response
import requests
import threading

app = Flask(__name__)

# Configuration via environment variables
NODE_NAME = os.environ.get("NODE_NAME", "cache1")
NODE_PORT = int(os.environ.get("NODE_PORT", "9527"))
ALL_NODES = os.environ.get("ALL_NODES", "cache1:9527,cache2:9528,cache3:9529")
# parse ALL_NODES into list of (name, host, port)
_nodes = []
for token in ALL_NODES.split(","):
    token = token.strip()
    if not token:
        continue
    if ":" in token:
        host, port = token.split(":",1)
        _nodes.append((host, host, int(port)))
    else:
        _nodes.append((token, token, 9527))

NODES = _nodes
NODENAME_TO_ADDR = {name: (host, port) for name, host, port in NODES}
# stable ordering
ORDERED_NODE_NAMES = [name for name,_,_ in NODES]

STORE = {}  # in-memory key->value

LOCK = threading.Lock()

def _owner_for_key(key: str) -> str:
    # simple hash-based consistent mapping: hash(key) % N
    h = int(hashlib.sha256(key.encode('utf-8')).hexdigest(), 16)
    idx = h % len(ORDERED_NODE_NAMES)
    return ORDERED_NODE_NAMES[idx]

def _make_node_url(node_name: str, path: str):
    host, port = NODENAME_TO_ADDR[node_name]
    return f"http://{host}:{port}{path}"

@app.route("/", methods=["POST"])
def write_kv():
    # Expect JSON body with a single key-value pair
    if not request.is_json:
        return Response("bad request: expect application/json", status=400)
    body = request.get_json()
    if not isinstance(body, dict) or len(body) != 1:
        return Response("bad request: expect single KV object", status=400)
    key = next(iter(body.keys()))
    value = body[key]
    owner = _owner_for_key(key)
    if owner == NODE_NAME:
        with LOCK:
            STORE[key] = value
        # return the stored KV as JSON
        return jsonify({key: value}), 200
    else:
        # forward to owner via internal RPC (HTTP)
        url = _make_node_url(owner, "/")
        try:
            resp = requests.post(url, json=body, headers={"Content-Type":"application/json"}, timeout=5)
        except requests.RequestException as e:
            return Response(f"error forwarding to owner: {e}", status=502)
        return Response(resp.content, status=resp.status_code, content_type=resp.headers.get("Content-Type","application/json"))

@app.route("/<path:key>", methods=["GET"])
def read_kv(key):
    owner = _owner_for_key(key)
    if owner == NODE_NAME:
        with LOCK:
            if key in STORE:
                return jsonify({key: STORE[key]}), 200
            else:
                return Response("", status=404)
    else:
        url = _make_node_url(owner, f"/{key}")
        try:
            resp = requests.get(url, timeout=5)
        except requests.RequestException as e:
            return Response(f"error fetching from owner: {e}", status=502)
        if resp.status_code == 200:
            return Response(resp.content, status=200, content_type=resp.headers.get("Content-Type","application/json"))
        elif resp.status_code == 404:
            return Response("", status=404)
        else:
            return Response(resp.content, status=resp.status_code, content_type=resp.headers.get("Content-Type","text/plain"))

@app.route("/<path:key>", methods=["DELETE"])
def delete_kv(key):
    owner = _owner_for_key(key)
    if owner == NODE_NAME:
        with LOCK:
            existed = 1 if key in STORE else 0
            if existed:
                del STORE[key]
        # return number deleted as plain text (per spec examples)
        return Response(str(existed), status=200, content_type="text/plain; charset=utf-8")
    else:
        url = _make_node_url(owner, f"/{key}")
        try:
            resp = requests.delete(url, timeout=5)
        except requests.RequestException as e:
            return Response(f"error deleting from owner: {e}", status=502)
        # propagate response (should be plain number)
        return Response(resp.content, status=resp.status_code, content_type=resp.headers.get("Content-Type","text/plain"))

@app.route("/_health", methods=["GET"])
def health():
    return jsonify({"node": NODE_NAME, "port": NODE_PORT, "keys": len(STORE)}), 200

if __name__ == "__main__":
    # allow overriding host binding; bind 0.0.0.0 inside container
    bind_host = "0.0.0.0"
    # Flask built-in server
    app.run(host=bind_host, port=NODE_PORT, threaded=True)
