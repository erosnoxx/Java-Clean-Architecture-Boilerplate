import os

IGNORE = {
    'target', '.idea', '.git', '__pycache__',
    '.mvn', '*.iml', '.DS_Store', 'node_modules'
}

def tree(path, prefix=''):
    entries = sorted(os.scandir(path), key=lambda e: (not e.is_dir(), e.name))
    entries = [e for e in entries if e.name not in IGNORE]

    for i, entry in enumerate(entries):
        connector = '└── ' if i == len(entries) - 1 else '├── '
        print(prefix + connector + entry.name)

        if not entry.is_dir():
            continue

        extension = '    ' if i == len(entries) - 1 else '│   '
        tree(entry.path, prefix + extension)

print(os.path.basename(os.getcwd()))
tree('../../')