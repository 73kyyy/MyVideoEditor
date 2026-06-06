import { create } from 'zustand';
import type { Clip } from '@/types';

interface ClipState {
  clips: Clip[];
  selectedClipId: string | null;
  selectClip: (id: string) => void;
  toggleClipSelection: (id: string) => void;
  updateClip: (id: string, updates: Partial<Clip>) => void;
}

const mockClips: Clip[] = [
  { id: 'c1', projectId: '1', startTime: 0, endTime: 15, name: 'Opening Hook', type: 'highlight', score: 96, tags: ['viral', 'hook'], selected: true },
  { id: 'c2', projectId: '1', startTime: 24, endTime: 42, name: 'Emotional Peak', type: 'emotion', score: 91, tags: ['emotion', 'peak'], selected: true },
  { id: 'c3', projectId: '1', startTime: 68, endTime: 85, name: 'Beat Drop', type: 'rhythm', score: 88, tags: ['rhythm', 'drop'], selected: false },
  { id: 'c4', projectId: '1', startTime: 120, endTime: 138, name: 'Scene Transition', type: 'scene', score: 82, tags: ['scene', 'transition'], selected: true },
  { id: 'c5', projectId: '1', startTime: 200, endTime: 218, name: 'Key Moment', type: 'highlight', score: 94, tags: ['viral', 'key'], selected: false },
  { id: 'c6', projectId: '1', startTime: 310, endTime: 330, name: 'Climax', type: 'emotion', score: 89, tags: ['emotion', 'climax'], selected: false },
  { id: 'c7', projectId: '1', startTime: 450, endTime: 470, name: 'Rhythm Sync', type: 'rhythm', score: 85, tags: ['rhythm', 'sync'], selected: false },
  { id: 'c8', projectId: '1', startTime: 600, endTime: 620, name: 'Closing Scene', type: 'scene', score: 78, tags: ['scene', 'closing'], selected: false },
];

export const useClipStore = create<ClipState>((set) => ({
  clips: mockClips,
  selectedClipId: 'c1',
  selectClip: (id) => set({ selectedClipId: id }),
  toggleClipSelection: (id) =>
    set((state) => ({
      clips: state.clips.map((c) => (c.id === id ? { ...c, selected: !c.selected } : c)),
    })),
  updateClip: (id, updates) =>
    set((state) => ({
      clips: state.clips.map((c) => (c.id === id ? { ...c, ...updates } : c)),
    })),
}));
