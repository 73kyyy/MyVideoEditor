export interface Project {
  id: string;
  name: string;
  thumbnail: string;
  duration: number;
  resolution: string;
  status: 'analyzing' | 'ready' | 'editing' | 'exported';
  aiScore: number;
  clipCount: number;
  createdAt: Date;
}

export interface Clip {
  id: string;
  projectId: string;
  startTime: number;
  endTime: number;
  name: string;
  type: 'emotion' | 'rhythm' | 'scene' | 'highlight';
  score: number;
  tags: string[];
  selected: boolean;
}

export interface AIAnalysis {
  projectId: string;
  sceneCount: number;
  emotionPeaks: number;
  rhythmBeats: number;
  sensitivity: number;
  emotionWeight: number;
  rhythmWeight: number;
  sceneFilter: string[];
  status: 'idle' | 'analyzing' | 'done';
  progress: number;
}

export interface ExportTask {
  id: string;
  clipIds: string[];
  platform: 'tiktok' | 'reels' | 'shorts' | 'custom';
  quality: '720p' | '1080p' | '4k';
  format: 'mp4' | 'mov';
  status: 'queued' | 'exporting' | 'done' | 'failed';
  progress: number;
}
