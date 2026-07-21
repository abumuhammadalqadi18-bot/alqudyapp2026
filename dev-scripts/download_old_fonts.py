import urllib.request
import os
import hashlib

os.makedirs('app/src/main/res/font', exist_ok=True)

urls = {
    'cairo_regular.ttf': 'https://raw.githubusercontent.com/google/fonts/e86936fd5b72936fe1eed526b69d49343a7d353b/ofl/cairo/Cairo-Regular.ttf',
    'cairo_semibold.ttf': 'https://raw.githubusercontent.com/google/fonts/e86936fd5b72936fe1eed526b69d49343a7d353b/ofl/cairo/Cairo-SemiBold.ttf',
    'cairo_bold.ttf': 'https://raw.githubusercontent.com/google/fonts/e86936fd5b72936fe1eed526b69d49343a7d353b/ofl/cairo/Cairo-Bold.ttf'
}

for name, url in urls.items():
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req) as response:
        data = response.read()
    
    path = os.path.join('app/src/main/res/font', name)
    with open(path, 'wb') as f:
        f.write(data)

    with open(path, 'rb') as f:
        d2 = f.read()
    
    count_efbfbd = d2.count(b'\xef\xbf\xbd')
    h = d2[:4].hex()
    m = hashlib.md5(d2).hexdigest()

    print(f"Path: {os.path.abspath(path)}")
    print(f"Size: {len(d2)}")
    print(f"First 4 bytes: {h}")
    print(f"EF BF BD count: {count_efbfbd}")
    print(f"MD5: {m}")
    print("-" * 20)
