import { create } from 'zustand';
import type { ExportTask } from '@/types';

interface ExportState {
  tasks: ExportTask[];
  addTask: (task: ExportTask) => void;
  updateTask: (id: string, updates: Partial<ExportTask>) => void;
}

const mockTasks: ExportTask[] = [
  {
    id: 'e1',
    clipIds: ['c1', 'c2', 'c4'],
    platform: 'tiktok',
    quality: '1080p',
    format: 'mp4',
    status: 'done',
    progress: 100,
  },
  {
    id: 'e2',
    clipIds: ['c3', 'c5'],
    platform: 'reels',
    quality: '1080p',
    format: 'mp4',
    status: 'exporting',
    progress: 67,
  },
  {
    id: 'e3',
    clipIds: ['c6'],
    platform: 'shorts',
    quality: '4k',
    format: 'mp4',
    status: 'queued',
    progress: 0,
  },
];

export const useExportStore = create<ExportState>((set) => ({
  tasks: mockTasks,
  addTask: (task) => set((state) => ({ tasks: [task, ...state.tasks] })),
  updateTask: (id, updates) =>
    set((state) => ({
      tasks: state.tasks.map((t) => (t.id === id ? { ...t, ...updates } : t)),
    })),
}));
