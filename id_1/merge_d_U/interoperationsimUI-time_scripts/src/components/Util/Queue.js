export class Queue {
    constructor() {
        this.items = {};
        this.headIndex = 0;
        this.tailIndex = 0;
    }

    enqueue(item) {
        this.items[this.tailIndex] = item;
        this.tailIndex++;
    }

    dequeue() {
        if (this.items.hasOwnProperty(this.headIndex)) {
            const item = this.items[this.headIndex];
            delete this.items[this.headIndex];
            this.headIndex++;
            return item;
        }
        return null
    }

    peek() {
        if (this.items.hasOwnProperty(this.headIndex)) {
            return this.items[this.headIndex];
        }
        return null
    }

    get length() {
        return this.tailIndex - this.headIndex;
    }

    list() {
        return Object.values(this.items)
    }
}
