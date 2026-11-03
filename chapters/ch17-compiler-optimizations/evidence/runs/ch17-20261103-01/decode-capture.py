from pathlib import Path
import re
import subprocess
root=Path(__file__).resolve().parent
text=(root/'stdout.txt').read_text()
# 只把本轮实际打印的C2机器码字节装入不可执行对象文件，交给系统反汇编器。
# 没有生成Java变体、断言或运行性能比较；相对跳转地址须加回原始基址理解。
records=[]
for block in re.split(r'(?=Compiled method \(c2\))',text):
    head=re.match(r'Compiled method \(c2\)[^\n]*OptimizationJourney::(\w+)',block)
    if not head:continue
    name=head.group(1)
    bounds=re.search(r'main code\s+\[0x([0-9a-f]+),0x([0-9a-f]+)\]',block)
    if not bounds:continue
    start,end=(int(x,16) for x in bounds.groups())
    asm=['.text','.p2align 2',f'.globl _capture_{name}',f'_capture_{name}:']
    byte_count=0
    for line in block.split('[/MachCode]')[0].splitlines():
        m=re.match(r'\s+0x([0-9a-f]+): ([0-9a-f |]+)(?:\s|$)',line)
        if not m:continue
        address=int(m.group(1),16)
        if not start<=address<end:continue
        raw=bytes.fromhex(m.group(2).replace('|',''))
        asm.extend([f'.org {address-start}', '.byte '+','.join(f'0x{b:02x}' for b in raw)])
        byte_count+=len(raw)
    tag=f'{name}-{start:x}'
    path=root/f'{tag}.s';path.write_text('\n'.join(asm)+'\n')
    obj=root/f'{tag}.o'
    subprocess.run(['/usr/bin/clang','-arch','arm64','-c',str(path),'-o',str(obj)],check=True)
    output=subprocess.run(['/usr/bin/otool','-tvV',str(obj)],capture_output=True,text=True,check=True)
    (root/f'{tag}-decoded.txt').write_text(output.stdout)
    records.append(f'{name}: original_base=0x{start:x}; main_end=0x{end:x}; captured_bytes={byte_count}; source={tag}.s; decoded={tag}-decoded.txt')
(root/'decode-index.txt').write_text('\n'.join(records)+'\n')
print('\n'.join(records))
