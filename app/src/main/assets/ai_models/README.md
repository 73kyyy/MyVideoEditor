# NexClip AI模型目录

## ONNX模型文件（CI/CD自动下载和导出）

| 模型 | 文件名 | 大小 | 功能 | 来源 |
|------|--------|------|------|------|
| Whisper Tiny Encoder | whisper_encoder.onnx | ~57MB | 语音识别编码器 | csukuangfj/sherpa-onnx-whisper-tiny |
| Whisper Tiny Decoder | whisper_decoder.onnx | ~3MB | 语音识别解码器 | csukuangfj/sherpa-onnx-whisper-tiny |
| Whisper Tokens | whisper_tokens.txt | ~90KB | BPE词表 | csukuangfj/sherpa-onnx-whisper-tiny |
| MobileSAM Encoder | sam_encoder.onnx | ~27MB | 目标分割编码器 | ChaoningZhang/MobileSAM |
| MobileSAM Decoder | sam_decoder.onnx | ~20MB | 目标分割解码器 | ChaoningZhang/MobileSAM |
| RealESRGAN x4plus | realesrgan_x4plus.onnx | ~67MB | 4倍超分辨率 | xinntao/Real-ESRGAN |
| RIFE v4.6 | rife_v4.onnx | ~17MB | 视频插帧 | yuvraj108c/rife-onnx |
| Demucs htdemucs | demucs_htdemucs.onnx | ~160MB | 音源分离(FP16) | StemSplitio/htdemucs-onnx |
| RNNoise | rnnoise.onnx | ~5MB | AI降噪 | xiph/rnnoise (真实架构) |

## 合计：约360MB

## 模型架构详情

### RNNoise (xiph/rnnoise真实架构)
- 输入: features [batch, frames, 65] (65个频带特征)
- 输出: gains [batch, frames-4, 32] (32个频带增益) + vad [batch, frames-4, 1] (语音活动检测)
- 架构: Conv1d(65→128) → Conv1d(128→256) → GRU×3(256) → Linear(1024→32) + Linear(1024→1)
- 参数量: 1,341,729

### RIFE v4.6
- 输入: img0 [batch,3,H,W] + img1 [batch,3,H,W] + timestep [batch]
- 输出: 插值帧 [batch,3,H,W]

### RealESRGAN x4plus
- 输入: [batch,3,H,W]
- 输出: [batch,3,H*4,W*4] (4倍超分)

### Whisper Tiny
- 编码器输入: mel_spectrogram [batch,80,3000]
- 编码器输出: [batch,1500,384]
- 解码器输入: tokens [batch,seq] + encoder_hidden_states [batch,1500,384]
- 解码器输出: logits [batch,seq,51865]

### MobileSAM
- 编码器输入: input_image [batch,3,1024,1024]
- 编码器输出: image_embeddings [batch,256,64,64]
- 解码器输入: image_embeddings + point_embeddings
- 解码器输出: low_res_masks + iou_predictions

### Demucs htdemucs
- 输入: audio [batch,2,samples] (立体声)
- 输出: stems [batch,4,2,samples] (drums/bass/other/vocals)

## 设备处理策略（效果一致，速度不同）
| 设备档位 | 线程数 | 分块大小 | GC间隔 |
|---------|--------|---------|--------|
| T1 低端机 | 2 | 256px | 每4块 |
| T2 中端机 | 4 | 512px | 不需要 |
| T3 旗舰机 | 8 | 1024px | 不需要 |

## 模型来源
- RNNoise: https://github.com/xiph/rnnoise
- RIFE: https://github.com/megvii-research/ECCV2022-RIFE
- MobileSAM: https://github.com/ChaoningZhang/MobileSAM
- RealESRGAN: https://github.com/xinntao/Real-ESRGAN
- Whisper: https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny
- Demucs: https://github.com/facebookresearch/demucs

## CI自动转换
build.yml会在GitHub Actions中自动下载、转换、打包这些模型。
所有模型必须通过大小验证，否则构建失败。
