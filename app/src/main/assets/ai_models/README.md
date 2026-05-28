# NexClip AI模型目录

## 方案A：全部打包进APK（624MB）

### FP32模型（5个）
| 模型 | 文件名 | 大小 | 功能 |
|------|--------|------|------|
| RNNoise | rnnoise_fp32.bin | 5MB | AI降噪 |
| RIFE-v4 | rife_v4_fp32.bin | 60MB | 视频插帧 |
| SAM2-base | sam2_base_fp32.bin | 156MB | 智能抠图 |
| RealESRGAN-x4 | esrgan_x4_fp32.bin | 128MB | 4倍超分辨率 |
| Whisper-tiny | whisper_tiny_fp32.bin | 75MB | 语音转文字 |

### FP16模型（1个）
| 模型 | 文件名 | 大小 | 功能 |
|------|--------|------|------|
| Demucs-ft | demucs_ft_fp16.bin | 160MB | 人声分离 |

### 合计：584MB

## 设备处理策略（效果一致，速度不同）
| 设备档位 | 线程数 | 分块大小 | GC间隔 |
|---------|--------|---------|--------|
| T1 低端机 | 2 | 256px | 每4块 |
| T2 中端机 | 4 | 512px | 不需要 |
| T3 旗舰机 | 8 | 1024px | 不需要 |

## 模型来源
- RNNoise: https://github.com/xiph/rnnoise
- RIFE: https://github.com/megvii-research/ECCV2022-RIFE
- SAM2: https://github.com/facebookresearch/sam2
- RealESRGAN: https://github.com/xinntao/Real-ESRGAN
- Whisper: https://huggingface.co/openai/whisper-tiny
- Demucs: https://github.com/facebookresearch/demucs

## CI自动转换
build.yml会在GitHub Actions中自动下载、转换、打包这些模型。
