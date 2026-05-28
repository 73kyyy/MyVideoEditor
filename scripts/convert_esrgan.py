import torch, sys
sys.path.append(".")
from realesrgan.archs.srvgg_arch import SRVGGNetCompact
model = SRVGGNetCompact(num_in_ch=3, num_out_ch=3, num_feat=64, num_block=23, num_grow_ch=32, scale=4)
model.load_state_dict(torch.load("weights/RealESRGAN_x4plus.pth")["params"], strict=True)
model.eval()
dummy = torch.randn(1, 3, 64, 64)
torch.onnx.export(model, dummy, "../esrgan_x4.onnx", opset_version=11)
print("RealESRGAN转换成功")
