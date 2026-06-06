import { create } from 'zustand';
import type { Settings } from '@/types';

interface SettingsState {
  settings: Settings;
  updateSetting: <K extends keyof Settings>(key: K, value: Settings[K]) => void;
  resetSettings: () => void;
}

const defaultSettings: Settings = {
  defaultQuality: '1080p',
  exportFormat: 'mp4',
  watermark: false,
  storageLocation: 'internal',
  gpuAcceleration: true,
  aiPrecision: 'fp16',
  darkMode: false,
  font: 'editorial',
};

export const useSettingsStore = create<SettingsState>((set) => ({
  settings: defaultSettings,
  updateSetting: (key, value) => set((state) => ({ settings: { ...state.settings, [key]: value } })),
  resetSettings: () => set({ settings: defaultSettings }),
}));
