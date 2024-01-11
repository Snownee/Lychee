# a simple script to append a hash to the end of a file name
import os
import hashlib

paths = ['docs/scripts', 'docs/stylesheets']

for path in paths:
    for filename in os.listdir(path):
        with open(os.path.join(path, filename), 'r') as f:
            data = f.read()
        hash = hashlib.md5(data.encode('utf-8')).hexdigest()[0:6]
        parts = filename.split('.')
        if len(parts) == 2:
            new_filename = parts[0] + '.' + hash + '.' + parts[1]
        elif len(parts) == 3:
            new_filename = parts[0] + '.' + hash + '.' + parts[2]
        else:
            print('Error: file name has more than two periods')
            exit()
        if filename == new_filename:
            print('Skipping ' + filename)
            continue
        os.rename(os.path.join(path, filename), os.path.join(path, new_filename))
        with open('mkdocs.yml', 'r') as f:
            data = f.read()
        data = data.replace(filename, new_filename)
        with open('mkdocs.yml', 'w') as f:
            f.write(data)
        print('Renamed ' + filename + ' to ' + new_filename)