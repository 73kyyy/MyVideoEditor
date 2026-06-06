import { create } from 'zustand';
import type { Project, AIFeature, ColorGradingTab, SpeedCurveType } from '@/types';

interface EditorState {
  currentProject: Project | null;
  currentTime: number;
  isPlaying: boolean;
  activePanel: string | null;
  activeTool: string | null;
  colorGradingTab: ColorGradingTab;
  speedCurveType: SpeedCurveType;
  aiFeatures: AIFeature[];
  setCurrentProject: (project: Project | null) => void;
  setCurrentTime: (time: number) => void;
  togglePlay: () => void;
  openPanel: (panel: string) => void;
  closePanel: () => void;
  setColorGradingTab: (tab: ColorGradingTab) => void;
  setSpeedCurveType: (type: SpeedCurveType) => void;
  toggleAIFeature: (id: string) => void;
}

const mockAIFeatures: AIFeature[] = [
  { id: 'interp', name: 'Smart Interpolation', description: 'RIFE · 30→60 FPS', model: 'RIFE', enabled: true, icon: '⇶' },
  { id: 'upscale', name: 'Super Resolution', description: 'Real-ESRGAN · HD→4K', model: 'RealESRGAN', enabled: false, icon: '↑' },
  { id: 'denoise', name: 'Audio Denoise', description: 'RNNoise · LIVE', model: 'RNNoise', enabled: true, icon: '∿' },
  { id: 'separate', name: 'Source Separation', description: 'Demucs · STEMS', model: 'Demucs', enabled: false, icon: '♫' },
  { id: 'caption', name: 'Auto Caption', description: 'Whisper · ASR', model: 'Whisper', enabled: true, icon: '¶' },
  { id: 'track', name: 'Object Tracking', description: 'SAM2 · MASKS', model: 'SAM2', enabled: false, icon: '◎' },
];

export const useEditorStore = create<EditorState>((set) => ({
  currentProject: null,
  currentTime: 8.12,
  isPlaying: false,
  activePanel: null,
  activeTool: null,
  colorGradingTab: 'basic',
  speedCurveType: 'linear',
  aiFeatures: mockAIFeatures,
  setCurrentProject: (project) => set({ currentProject: project }),
  setCurrentTime: (time) => set({ currentTime: time }),
  togglePlay: () => set((state) => ({ isPlaying: !state.isPlaying })),
  openPanel: (panel) => set({ activePanel: panel }),
  closePanel: () => set({ activePanel: null, activeTool: null }),
  setColorGradingTab: (tab) => set({ colorGradingTab: tab }),
  setSpeedCurveType: (type) => set({ speedCurveType: type }),
  toggleAIFeature: (id) =>
    set((state) => ({
      aiFeatures: state.aiFeatures.map((f) => (f.id === id ? { ...f, enabled: !f.enabled } : f)),
    })),
}));
