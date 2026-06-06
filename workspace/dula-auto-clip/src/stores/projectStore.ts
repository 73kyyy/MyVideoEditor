import { create } from 'zustand';
import type { Project } from '@/types';

interface ProjectState {
  projects: Project[];
  addProject: (project: Project) => void;
  removeProject: (id: string) => void;
  updateProject: (id: string, updates: Partial<Project>) => void;
}

const mockProjects: Project[] = [
  {
    id: '1',
    name: 'Summer Vlog 2026',
    thumbnail: 'linear-gradient(135deg, #00F0FF, #8B5CF6)',
    duration: 847,
    resolution: '4K',
    status: 'ready',
    aiScore: 92,
    clipCount: 12,
    createdAt: new Date(),
  },
  {
    id: '2',
    name: 'Product Launch Reel',
    thumbnail: 'linear-gradient(135deg, #FF6B35, #FF8F5E)',
    duration: 324,
    resolution: '1080p',
    status: 'editing',
    aiScore: 87,
    clipCount: 8,
    createdAt: new Date(),
  },
  {
    id: '3',
    name: 'City Night Walk',
    thumbnail: 'linear-gradient(135deg, #8B5CF6, #00F0FF)',
    duration: 512,
    resolution: '4K',
    status: 'analyzing',
    aiScore: 0,
    clipCount: 0,
    createdAt: new Date(),
  },
  {
    id: '4',
    name: 'Interview Raw',
    thumbnail: 'linear-gradient(135deg, #3A3A44, #6B6B76)',
    duration: 1800,
    resolution: '1080p',
    status: 'ready',
    aiScore: 74,
    clipCount: 6,
    createdAt: new Date(),
  },
  {
    id: '5',
    name: 'Travel Montage',
    thumbnail: 'linear-gradient(135deg, #00C4D4, #7C3AED)',
    duration: 643,
    resolution: '4K',
    status: 'exported',
    aiScore: 95,
    clipCount: 15,
    createdAt: new Date(),
  },
  {
    id: '6',
    name: 'Cooking Tutorial',
    thumbnail: 'linear-gradient(135deg, #FF8F5E, #FF6B35)',
    duration: 420,
    resolution: '1080p',
    status: 'ready',
    aiScore: 81,
    clipCount: 9,
    createdAt: new Date(),
  },
];

export const useProjectStore = create<ProjectState>((set) => ({
  projects: mockProjects,
  addProject: (project) => set((state) => ({ projects: [project, ...state.projects] })),
  removeProject: (id) => set((state) => ({ projects: state.projects.filter((p) => p.id !== id) })),
  updateProject: (id, updates) =>
    set((state) => ({
      projects: state.projects.map((p) => (p.id === id ? { ...p, ...updates } : p)),
    })),
}));
