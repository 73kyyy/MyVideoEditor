import { create } from 'zustand';
import type { Project } from '@/types';

interface ProjectState {
  projects: Project[];
  activeTab: 'all' | 'draft' | 'done' | 'ai';
  addProject: (project: Project) => void;
  deleteProject: (id: string) => void;
  setActiveTab: (tab: 'all' | 'draft' | 'done' | 'ai') => void;
}

const mockProjects: Project[] = [
  {
    id: '1',
    name: 'City Nights',
    thumbnail: 'linear-gradient(135deg,#FF3D00,#FFD600)',
    duration: 154,
    resolution: '1080p',
    fps: 60,
    aiEnhanced: true,
    aiFeatures: ['interpolation', 'upscale'],
    status: 'done',
    createdAt: new Date(),
    updatedAt: new Date(),
  },
  {
    id: '2',
    name: 'Travelogue',
    thumbnail: 'linear-gradient(135deg,#1A6B3F,#0EA5E9)',
    duration: 312,
    resolution: '4K',
    fps: 30,
    aiEnhanced: false,
    aiFeatures: [],
    status: 'done',
    createdAt: new Date(),
    updatedAt: new Date(),
  },
  {
    id: '3',
    name: 'Product Reel',
    thumbnail: 'linear-gradient(135deg,#7C3AED,#FF3D00)',
    duration: 108,
    resolution: '1080p',
    fps: 30,
    aiEnhanced: true,
    aiFeatures: ['caption', 'denoise'],
    status: 'done',
    createdAt: new Date(),
    updatedAt: new Date(),
  },
  {
    id: '4',
    name: 'Untitled Draft',
    thumbnail: 'linear-gradient(135deg,#0A0A0A,#3A3A38)',
    duration: 32,
    resolution: '1080p',
    fps: 30,
    aiEnhanced: false,
    aiFeatures: [],
    status: 'draft',
    createdAt: new Date(),
    updatedAt: new Date(),
  },
];

export const useProjectStore = create<ProjectState>((set) => ({
  projects: mockProjects,
  activeTab: 'all',
  addProject: (project) => set((state) => ({ projects: [project, ...state.projects] })),
  deleteProject: (id) => set((state) => ({ projects: state.projects.filter((p) => p.id !== id) })),
  setActiveTab: (tab) => set({ activeTab: tab }),
}));
