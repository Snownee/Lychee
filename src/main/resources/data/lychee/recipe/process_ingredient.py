import os
from ruamel.yaml import YAML

def process_node(node):
    """
    递归处理 YAML 节点，同时保留 ruamel.yaml 附加在对象上的格式属性
    """
    if isinstance(node, dict):
        # 1. 先递归处理所有子节点
        for k in list(node.keys()):
            node[k] = node[k] if k == 'ingredient' else process_node(node[k])

        # 2. 检查规则
        keys = set(node.keys())
        
        # 规则 1 & 2: 只有一个成员的情况
        if len(keys) == 1:
            if 'item' in keys and isinstance(node['item'], str):
                return node['item']
            if 'tag' in keys and isinstance(node['tag'], str):
                return f"#{node['tag']}"

        # 规则 3 & 4: 包含 item/tag 且包含 count 的情况
        if 'count' in keys:
            if 'item' in keys and isinstance(node['item'], str):
                # ruamel.yaml 的字典支持 pop 但会丢失部分元数据
                # 简单替换键值对
                val = node.pop('item')
                node['ingredient'] = val
            elif 'tag' in keys and isinstance(node['tag'], str):
                val = node.pop('tag')
                node['ingredient'] = f"#{val}"
        
        return node

    elif isinstance(node, list):
        # 列表必须就地修改或保持其特殊类型，以保留 [ ] 格式
        for i in range(len(node)):
            node[i] = process_node(node[i])
        return node
    
    return node

def main():
    # 关键配置：typ='rt' (Round Trip) 是保留格式的核心
    yaml = YAML(typ='rt') 
    
    # 告诉加载器：保留原始的流式/块状风格
    yaml.preserve_quotes = True 
    # 强制尝试保持原始风格
    yaml.default_flow_style = None 

    for root, dirs, files in os.walk('.'):
        for file in files:
            if file.endswith(('.yaml', '.yml')):
                file_path = os.path.join(root, file)
                
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        data = yaml.load(f)
                    
                    if data is None: continue

                    new_data = process_node(data)

                    with open(file_path, 'w', encoding='utf-8') as f:
                        yaml.dump(new_data, f)
                    
                    print(f"✅ 已处理: {file_path}")
                except Exception as e:
                    print(f"❌ 出错 {file_path}: {e}")

if __name__ == "__main__":
    main()