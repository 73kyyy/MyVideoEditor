import { create } from 'zustand';
import type { AIAnalysis } from '@/types';

interface AIState {
  analysis: AIAnalysis;
  setSensitivity: (value: number) => void;
  setEmotionWeight: (value: number) => void;
  setRhythmWeight: (value: number) => void;
  startAnalysis: () => void;
}

const defaultAnalysis: AIAnalysis = {
  projectId: '1',
  sceneCount: 24,
  emotionPeaks: 8,
  rhythmBeats: 36,
  sensitivity: 70,
  emotionWeight: 60,
  rhythmWeight: 50,
  sceneFilter: [],
  status: 'done',
  progress: 100,
};

export const useAIStore = create<AIState>((set) => ({
  analysis: defaultAnalysis,
  setSensitivity: (value) =>
    set((state) => ({ analysis: { ...state.analysis, sensitivity: value } })),
  setEmotionWeight: (value) =>
    set((state) => ({ analysis: { ...state.analysis, emotionWeight: value } })),
  setRhythmWeight: (value) =>
    set((state) => ({ analysis: { ...state.analysis, rhythmWeight: value } })),
  startAnalysis: () =>
    set((state) => ({ analysis: { ...state.analysis, status: 'analyzing', progress: 0 } })),
}));
