import torch, sys
sys.path.append(".")
from demucs.pretrained import get_model
model = get_model("htdemucs_ft")
model.eval()
dummy = torch.randn(1, 4, 44100*10)
torch.onnx.export(model, dummy, "../demucs_ft.onnx", opset_version=11)
print("Demucs转换成功")
