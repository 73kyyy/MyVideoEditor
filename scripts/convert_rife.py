import torch, sys
sys.path.append(".")
from model.RIFE import Model
model = Model()
model.load_model("train_log/RIFE_net.pth")
model.eval()
d1 = torch.randn(1, 3, 256, 256)
d2 = torch.randn(1, 3, 256, 256)
torch.onnx.export(model, (d1, d2), "../rife_v4.onnx", opset_version=11)
print("RIFE转换成功")
