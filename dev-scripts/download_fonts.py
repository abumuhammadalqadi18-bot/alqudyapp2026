import urllib.request
import os
import hashlib

os.makedirs('app/src/main/res/font', exist_ok=True)

urls = {
    'cairo_regular.ttf': 'https://fonts.gstatic.com/s/cairo/v31/SLXgc1nY6HkvangtZmpQdkhzfH5lkSs2SgRjCAGMQ1z0hOA-a1PiKS2EikE.ttf',
    'cairo_semibold.ttf': 'https://fonts.gstatic.com/s/cairo/v31/SLXgc1nY6HkvangtZmpQdkhzfH5lkSs2SgRjCAGMQ1z0hD45a1PiKS2EikE.ttf',
    'cairo_bold.ttf': 'https://fonts.gstatic.com/s/cairo/v31/SLXgc1nY6HkvangtZmpQdkhzfH5lkSs2SgRjCAGMQ1z0hAc5a1PiKS2EikE.ttf'
}

for name, url in urls.items():
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 (Linux; U; Android 2.2-update1; en-us; DROID2 GLOBAL Build/S273) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1'})
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
