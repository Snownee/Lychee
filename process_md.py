# copy all the files in `original_docs` folder to `docs` folder and process `md` files in `docs` folder

import os
import shutil
import re
import json
import yaml
from pathlib import Path

excludes = ['fragment.md']

def main():
    # clear the `docs` folder
    shutil.rmtree('docs')
    os.makedirs('docs', exist_ok=True)

    # copy all the files in `original_docs` folder to `docs` folder
    for (dirpath, dirnames, filenames) in os.walk('original_docs'):
        for filename in filenames:
            src = os.path.join(dirpath, filename)
            dst = src.replace('original_docs', 'docs')
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            standard_path = Path(dst).as_posix()[5:]
            if filename.endswith('.md') and standard_path not in excludes:
                print(f'Processing {standard_path}...')
                with open(src, 'r', encoding='utf-8') as f:
                    data = f.read()
                data = processMD(data)
                with open(dst, 'w', encoding='utf-8') as f:
                    f.write(data)
            else:
                shutil.copy(src, dst)
    print('Copied all the files in `original_docs` folder to `docs` folder')

def processMD(data):
    pattern = re.compile(r'^([\t ]*)```json( [^\n]+)?(.+?)```', re.MULTILINE | re.DOTALL)
    data = pattern.sub(processJsonBlock, data)
    return data

def processJsonBlock(match):
    indent = match.group(1)
    attrs = match.group(2) or ''
    json_str = match.group(3)
    # print(json_str)
    json_obj = json.loads(json_str)
    yaml_str = yaml.safe_dump(json_obj, default_flow_style=False, indent=2, sort_keys=False, explicit_end=False)
    if yaml_str.endswith('\n...\n'):
        yaml_str = yaml_str[:-5]
    yaml_str = indent + yaml_str.strip()
    yaml_str = yaml_str.replace('\n', '\n' + indent)
    attrs = attrs.replace('.json', '.yaml')
    yaml_str = indent + '```yaml' + attrs + '\n' + yaml_str + '\n' + indent + '```'
    json_str = match.group(0)
    indent_type = '    ' if indent.startswith(' ') else '\t'
    yaml_str = re.compile(r'^', re.MULTILINE).sub(indent_type, yaml_str)
    json_str = re.compile(r'^', re.MULTILINE).sub(indent_type, json_str)
    s = indent + '===! "YAML"\n\n' + yaml_str + '\n\n' + indent + '=== "JSON"\n\n' + json_str
    return s

if __name__ == '__main__':
    main()
