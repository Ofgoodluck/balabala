# GIS地理信息系统模块

import folium
import json
from typing import List, Dict, Any, Tuple
from geopy.distance import geodesic
from datetime import datetime

from config import DATABASE_NODES, GIS_CONFIG

class GISManager:
    """GIS地理信息管理器"""
    
    def __init__(self):
        self.libraries_data = self.load_libraries_data()
        self.map_cache = {}
        
    def load_libraries_data(self) -> List[Dict[str, Any]]:
        """加载图书馆地理数据"""
        libraries = []
        
        for node_id, config in DATABASE_NODES.items():
            library_data = {
                'id': node_id,
                'name': config['name'],
                'address': config['address'],
                'latitude': config['location']['lat'],
                'longitude': config['location']['lng'],
                'contact_phone': '028-85405110',  # 示例电话
                'opening_hours': '08:00-22:00',
                'services': ['图书借阅', '电子资源', '学习空间', '文献传递'],
                'specialties': self.get_library_specialties(node_id)
            }
            libraries.append(library_data)
            
        return libraries
    
    def get_library_specialties(self, node_id: str) -> List[str]:
        """获取图书馆专业特色"""
        specialties_map = {
            'node1': ['文学', '历史', '哲学', '艺术', '综合性藏书'],  # 四川大学
            'node2': ['计算机科学', '电子工程', '通信技术', '人工智能', '信息技术'],  # 电子科技大学
            'node3': ['交通运输', '土木工程', '机械工程', '建筑学', '工程技术']   # 西南交通大学
        }
        return specialties_map.get(node_id, ['综合'])
    
    def create_interactive_map(self, highlighted_library: str = None) -> str:
        """创建交互式地图"""
        # 创建地图中心点（成都市中心）
        center_lat = GIS_CONFIG['center_lat']
        center_lng = GIS_CONFIG['center_lng']
        
        m = folium.Map(
            location=[center_lat, center_lng],
            zoom_start=GIS_CONFIG['zoom_level'],
            tiles='OpenStreetMap'
        )
        
        # 添加图书馆标记
        for library in self.libraries_data:
            self.add_library_marker(m, library, highlighted_library)
        
        # 添加连接线显示网络
        self.add_network_connections(m)
        
        # 添加图例
        self.add_map_legend(m)
        
        # 返回HTML字符串
        return m._repr_html_()
    
    def add_library_marker(self, map_obj: folium.Map, library: Dict[str, Any], highlighted: str = None):
        """添加图书馆标记"""
        # 确定标记颜色和图标
        if highlighted and library['id'] == highlighted:
            color = 'red'
            icon = 'star'
        else:
            color = 'blue'
            icon = 'book'
        
        # 创建弹出窗口内容
        popup_html = self.create_popup_content(library)
        
        # 添加标记
        folium.Marker(
            location=[library['latitude'], library['longitude']],
            popup=folium.Popup(popup_html, max_width=300),
            tooltip=library['name'],
            icon=folium.Icon(color=color, icon=icon, prefix='fa')
        ).add_to(map_obj)
        
        # 添加圆形覆盖区域表示服务范围
        folium.Circle(
            location=[library['latitude'], library['longitude']],
            radius=2000,  # 2公里服务半径
            popup=f"{library['name']} 服务范围",
            color=color,
            fill=True,
            fillOpacity=0.1
        ).add_to(map_obj)
    
    def create_popup_content(self, library: Dict[str, Any]) -> str:
        """创建弹出窗口内容"""
        specialties_str = ", ".join(library['specialties'])
        services_str = ", ".join(library['services'])
        
        html = f"""
        <div style="width: 280px;">
            <h4 style="margin: 0; color: #2c3e50;">{library['name']}</h4>
            <hr style="margin: 5px 0;">
            
            <p style="margin: 3px 0;"><strong>地址:</strong> {library['address']}</p>
            <p style="margin: 3px 0;"><strong>联系电话:</strong> {library['contact_phone']}</p>
            <p style="margin: 3px 0;"><strong>开放时间:</strong> {library['opening_hours']}</p>
            
            <h5 style="margin: 8px 0 3px 0; color: #34495e;">专业特色:</h5>
            <p style="margin: 0; font-size: 12px; color: #7f8c8d;">{specialties_str}</p>
            
            <h5 style="margin: 8px 0 3px 0; color: #34495e;">提供服务:</h5>
            <p style="margin: 0; font-size: 12px; color: #7f8c8d;">{services_str}</p>
            
            <div style="margin-top: 8px; text-align: center;">
                <button onclick="showLibraryDetails('{library['id']}')" 
                        style="background: #3498db; color: white; border: none; padding: 5px 10px; border-radius: 3px; cursor: pointer;">
                    查看详情
                </button>
            </div>
        </div>
        """
        return html
    
    def add_network_connections(self, map_obj: folium.Map):
        """添加网络连接线"""
        # 绘制三角形连接（每个图书馆都与其他图书馆相连）
        for i, lib1 in enumerate(self.libraries_data):
            for j, lib2 in enumerate(self.libraries_data[i+1:], i+1):
                # 绘制连接线
                folium.PolyLine(
                    locations=[
                        [lib1['latitude'], lib1['longitude']],
                        [lib2['latitude'], lib2['longitude']]
                    ],
                    color='green',
                    weight=2,
                    opacity=0.6,
                    popup=f"网络连接: {lib1['name']} ↔ {lib2['name']}"
                ).add_to(map_obj)
    
    def add_map_legend(self, map_obj: folium.Map):
        """添加地图图例"""
        legend_html = '''
        <div style="position: fixed; 
                    bottom: 50px; left: 50px; width: 200px; height: 120px; 
                    background-color: white; border:2px solid grey; z-index:9999; 
                    font-size:14px; padding: 10px">
        
        <h4 style="margin-top: 0;">图例</h4>
        <p><i class="fa fa-book" style="color:blue"></i> 图书馆</p>
        <p><i class="fa fa-star" style="color:red"></i> 当前选中图书馆</p>
        <p><span style="color:green; font-weight:bold;">━━━</span> 网络连接</p>
        <p><span style="color:blue; opacity:0.3;">●</span> 服务范围 (2km)</p>
        
        </div>
        '''
        map_obj.get_root().html.add_child(folium.Element(legend_html))
    
    def calculate_distance(self, lib1_id: str, lib2_id: str) -> float:
        """计算两个图书馆之间的距离"""
        lib1 = next((lib for lib in self.libraries_data if lib['id'] == lib1_id), None)
        lib2 = next((lib for lib in self.libraries_data if lib['id'] == lib2_id), None)
        
        if lib1 and lib2:
            coord1 = (lib1['latitude'], lib1['longitude'])
            coord2 = (lib2['latitude'], lib2['longitude'])
            distance = geodesic(coord1, coord2).kilometers
            return round(distance, 2)
        
        return 0.0
    
    def find_nearest_libraries(self, target_lat: float, target_lng: float, limit: int = 3) -> List[Dict[str, Any]]:
        """查找最近的图书馆"""
        target_coord = (target_lat, target_lng)
        
        libraries_with_distance = []
        for library in self.libraries_data:
            lib_coord = (library['latitude'], library['longitude'])
            distance = geodesic(target_coord, lib_coord).kilometers
            
            library_copy = library.copy()
            library_copy['distance'] = round(distance, 2)
            libraries_with_distance.append(library_copy)
        
        # 按距离排序
        libraries_with_distance.sort(key=lambda x: x['distance'])
        
        return libraries_with_distance[:limit]
    
    def get_library_coverage_analysis(self) -> Dict[str, Any]:
        """获取图书馆覆盖分析"""
        analysis = {
            'total_libraries': len(self.libraries_data),
            'coverage_area': '成都市主要高校区域',
            'network_connections': 0,
            'average_distance': 0.0,
            'service_overlap': []
        }
        
        # 计算网络连接数
        n = len(self.libraries_data)
        analysis['network_connections'] = n * (n - 1) // 2
        
        # 计算平均距离
        distances = []
        for i, lib1 in enumerate(self.libraries_data):
            for j, lib2 in enumerate(self.libraries_data[i+1:], i+1):
                distance = self.calculate_distance(lib1['id'], lib2['id'])
                distances.append(distance)
        
        if distances:
            analysis['average_distance'] = round(sum(distances) / len(distances), 2)
        
        # 分析服务重叠
        for i, lib1 in enumerate(self.libraries_data):
            for j, lib2 in enumerate(self.libraries_data[i+1:], i+1):
                distance = self.calculate_distance(lib1['id'], lib2['id'])
                if distance < 4.0:  # 服务半径重叠
                    analysis['service_overlap'].append({
                        'library1': lib1['name'],
                        'library2': lib2['name'], 
                        'distance': distance,
                        'overlap_level': '高' if distance < 2.0 else '中'
                    })
        
        return analysis
    
    def create_heatmap(self, activity_data: List[Dict[str, Any]] = None) -> str:
        """创建活动热力图"""
        m = folium.Map(
            location=[GIS_CONFIG['center_lat'], GIS_CONFIG['center_lng']],
            zoom_start=GIS_CONFIG['zoom_level']
        )
        
        # 如果没有提供活动数据，使用模拟数据
        if not activity_data:
            activity_data = self.generate_mock_activity_data()
        
        # 准备热力图数据
        heat_data = []
        for activity in activity_data:
            heat_data.append([
                activity['latitude'],
                activity['longitude'],
                activity['intensity']
            ])
        
        # 添加热力图层
        from folium.plugins import HeatMap
        HeatMap(heat_data).add_to(m)
        
        # 添加图书馆标记
        for library in self.libraries_data:
            folium.Marker(
                location=[library['latitude'], library['longitude']],
                popup=library['name'],
                icon=folium.Icon(color='blue', icon='book')
            ).add_to(m)
        
        return m._repr_html_()
    
    def generate_mock_activity_data(self) -> List[Dict[str, Any]]:
        """生成模拟活动数据"""
        import random
        
        activities = []
        for library in self.libraries_data:
            # 在图书馆周围生成随机活动点
            for _ in range(20):
                lat_offset = random.uniform(-0.01, 0.01)
                lng_offset = random.uniform(-0.01, 0.01)
                
                activities.append({
                    'latitude': library['latitude'] + lat_offset,
                    'longitude': library['longitude'] + lng_offset,
                    'intensity': random.uniform(0.3, 1.0),
                    'library_id': library['id']
                })
        
        return activities
    
    def get_spatial_query_results(self, query_type: str, params: Dict[str, Any]) -> List[Dict[str, Any]]:
        """执行空间查询"""
        results = []
        
        if query_type == 'nearest_libraries':
            results = self.find_nearest_libraries(
                params['latitude'], 
                params['longitude'], 
                params.get('limit', 3)
            )
        
        elif query_type == 'libraries_in_radius':
            target_coord = (params['latitude'], params['longitude'])
            radius = params.get('radius', 5.0)  # 默认5公里
            
            for library in self.libraries_data:
                lib_coord = (library['latitude'], library['longitude'])
                distance = geodesic(target_coord, lib_coord).kilometers
                
                if distance <= radius:
                    library_copy = library.copy()
                    library_copy['distance'] = round(distance, 2)
                    results.append(library_copy)
        
        elif query_type == 'route_planning':
            # 简单的路径规划
            start_lib = next((lib for lib in self.libraries_data if lib['id'] == params['start']), None)
            end_lib = next((lib for lib in self.libraries_data if lib['id'] == params['end']), None)
            
            if start_lib and end_lib:
                distance = self.calculate_distance(params['start'], params['end'])
                results = [{
                    'start_library': start_lib['name'],
                    'end_library': end_lib['name'],
                    'distance': distance,
                    'estimated_time': round(distance * 3, 1)  # 假设平均速度20km/h
                }]
        
        return results
    
    def export_gis_data(self, format_type: str = 'geojson') -> str:
        """导出GIS数据"""
        if format_type == 'geojson':
            features = []
            
            for library in self.libraries_data:
                feature = {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [library['longitude'], library['latitude']]
                    },
                    "properties": {
                        "id": library['id'],
                        "name": library['name'],
                        "address": library['address'],
                        "specialties": library['specialties'],
                        "services": library['services']
                    }
                }
                features.append(feature)
            
            geojson_data = {
                "type": "FeatureCollection",
                "features": features
            }
            
            return json.dumps(geojson_data, ensure_ascii=False, indent=2)
        
        return "Unsupported format type"



