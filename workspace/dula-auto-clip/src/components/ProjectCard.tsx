import { Clock, Star } from 'lucide-react';
import ScoreRing from './ScoreRing';
import type { Project } from '@/types';

interface ProjectCardProps {
  project: Project;
  onClick: (project: Project) => void;
}

const statusConfig: Record<string, { label: string; color: string; bg: string }> = {
  analyzing: { label: 'ANALYZING', color: 'text-heat', bg: 'bg-heat/10 border-heat/30' },
  ready: { label: 'READY', color: 'text-neon', bg: 'bg-neon/10 border-neon/30' },
  editing: { label: 'EDITING', color: 'text-violet', bg: 'bg-violet/10 border-violet/30' },
  exported: { label: 'EXPORTED', color: 'text-muted', bg: 'bg-surface3 border-dim/30' },
};

function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

export default function ProjectCard({ project, onClick }: ProjectCardProps) {
  const status = statusConfig[project.status];

  return (
    <div
      onClick={() => onClick(project)}
      className="group glass-light rounded-xl border border-dim/30 overflow-hidden cursor-pointer hover:border-neon/30 hover:shadow-neon transition-all duration-300 hover:-translate-y-1"
    >
      {/* Thumbnail */}
      <div className="h-32 relative overflow-hidden">
        <div className="absolute inset-0" style={{ background: project.thumbnail }} />
        <div className="absolute inset-0 bg-gradient-to-t from-void/80 to-transparent" />

        {/* Status Badge */}
        <div className={`absolute top-3 right-3 px-2 py-0.5 rounded-md border font-mono text-[9px] tracking-wider font-semibold ${status.bg} ${status.color}`}>
          {status.label}
        </div>

        {/* Duration */}
        <div className="absolute bottom-3 left-3 flex items-center gap-1 font-mono text-[10px] text-paper/80">
          <Clock className="w-3 h-3" />
          {formatDuration(project.duration)}
        </div>

        {/* Resolution */}
        <div className="absolute bottom-3 right-3 font-mono text-[10px] text-paper/60">
          {project.resolution}
        </div>
      </div>

      {/* Info */}
      <div className="p-4">
        <div className="flex items-start justify-between gap-3">
          <div className="flex-1 min-w-0">
            <h3 className="font-grotesk font-semibold text-sm text-paper truncate group-hover:text-neon transition-colors">
              {project.name}
            </h3>
            <div className="flex items-center gap-2 mt-1.5">
              <span className="font-mono text-[10px] text-muted">
                {project.clipCount} clips
              </span>
              {project.aiScore > 0 && (
                <div className="flex items-center gap-0.5">
                  <Star className="w-3 h-3 text-heat" />
                  <span className="font-mono text-[10px] text-heat font-semibold">{project.aiScore}</span>
                </div>
              )}
            </div>
          </div>
          {project.aiScore > 0 && <ScoreRing score={project.aiScore} size={40} strokeWidth={2.5} />}
        </div>

        {/* AI Score Heat Bar */}
        {project.aiScore > 0 && (
          <div className="mt-3 h-1 bg-surface3 rounded-full overflow-hidden">
            <div
              className="h-full rounded-full transition-all duration-700"
              style={{
                width: `${project.aiScore}%`,
                background: project.aiScore >= 90
                  ? 'linear-gradient(90deg, #00F0FF, #8B5CF6)'
                  : project.aiScore >= 80
                  ? 'linear-gradient(90deg, #8B5CF6, #FF8F5E)'
                  : 'linear-gradient(90deg, #FF8F5E, #FF6B35)',
              }}
            />
          </div>
        )}
      </div>
    </div>
  );
}
