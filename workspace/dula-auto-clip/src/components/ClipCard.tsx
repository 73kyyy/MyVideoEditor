import type { Clip } from '@/types';
import ScoreRing from './ScoreRing';

interface ClipCardProps {
  clip: Clip;
  isSelected: boolean;
  isHighlighted: boolean;
  onSelect: (id: string) => void;
}

const typeConfig: Record<string, { label: string; color: string; bg: string; border: string }> = {
  emotion: { label: 'EMOTION', color: 'text-heat', bg: 'bg-heat/10', border: 'border-heat/30' },
  rhythm: { label: 'RHYTHM', color: 'text-neon', bg: 'bg-neon/10', border: 'border-neon/30' },
  scene: { label: 'SCENE', color: 'text-violet', bg: 'bg-violet/10', border: 'border-violet/30' },
  highlight: { label: 'HIGHLIGHT', color: 'text-heat2', bg: 'bg-heat2/10', border: 'border-heat2/30' },
};

function formatTime(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

export default function ClipCard({ clip, isSelected, isHighlighted, onSelect }: ClipCardProps) {
  const type = typeConfig[clip.type];

  return (
    <div
      onClick={() => onSelect(clip.id)}
      className={`p-3 rounded-lg border cursor-pointer transition-all duration-200 ${
        isSelected
          ? 'bg-neon/5 border-neon/40 shadow-neon'
          : isHighlighted
          ? 'bg-surface2/60 border-dim/40'
          : 'bg-surface/40 border-dim/20 hover:border-dim/40'
      }`}
    >
      <div className="flex items-center gap-3">
        <ScoreRing score={clip.score} size={36} strokeWidth={2.5} />
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1">
            <span className={`font-grotesk font-medium text-sm ${isSelected ? 'text-neon' : 'text-paper'} truncate`}>
              {clip.name}
            </span>
          </div>
          <div className="flex items-center gap-2">
            <span className={`px-1.5 py-0.5 rounded font-mono text-[8px] tracking-wider font-semibold ${type.bg} ${type.border} border ${type.color}`}>
              {type.label}
            </span>
            <span className="font-mono text-[10px] text-muted">
              {formatTime(clip.startTime)} – {formatTime(clip.endTime)}
            </span>
          </div>
        </div>
        <div className="flex items-center gap-1">
          {clip.tags.map((tag) => (
            <span key={tag} className="px-1.5 py-0.5 rounded bg-surface3 font-mono text-[8px] text-muted tracking-wider">
              {tag}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}
