export interface Project {
  id: string;
  name: string;
  thumbnail: string;
  duration: number;
  resolution: string;
  fps: number;
  aiEnhanced: boolean;
  aiFeatures: string[];
  status: 'draft' | 'done';
  createdAt: Date;
  updatedAt: Date;
}

export interface Clip {
  id: string;
  start: number;
  end: number;
  name: string;
  style?: 'solid' | 'dashed';
}

export interface Track {
  id: string;
  type: 'video' | 'audio' | 'text';
  label: string;
  clips: Clip[];
}

export interface AIFeature {
  id: string;
  name: string;
  description: string;
  model: string;
  enabled: boolean;
  icon: string;
}

export interface Settings {
  defaultQuality: '720p' | '1080p' | '4K';
  exportFormat: 'mp4' | 'mov';
  watermark: boolean;
  storageLocation: 'internal' | 'external';
  gpuAcceleration: boolean;
  aiPrecision: 'fp32' | 'fp16' | 'int8';
  darkMode: boolean;
  font: 'editorial' | 'modern';
}

export type ColorGradingTab = 'basic' | 'curve' | 'wheel' | 'hsl';
export type SpeedCurveType = 'linear' | 'in' | 'out' | 'both' | 'bounce' | 'custom';
