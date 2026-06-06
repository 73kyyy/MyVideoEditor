import { useClipStore } from '@/stores/clipStore';

export default function ClipTimeline() {
  const { clips, selectedClipId } = useClipStore();
  const totalDuration = 847;

  return (
    <div className="glass-light rounded-xl border border-dim/30 p-4">
      {/* Header */}
      <div className="flex items-center justify-between mb-3">
        <span className="font-mono text-[10px] text-muted tracking-wider uppercase">
          Timeline · 14:07
        </span>
        <span className="font-mono text-[10px] text-neon font-semibold tracking-wider">
          ZOOM 100%
        </span>
      </div>

      {/* Main Track */}
      <div className="relative h-10 bg-surface3 rounded-md overflow-hidden mb-2">
        <div className="absolute inset-0 opacity-20"
          style={{
            background: 'repeating-linear-gradient(90deg, transparent, transparent 24px, rgba(0,240,255,0.05) 24px, rgba(0,240,255,0.05) 25px)',
          }}
        />
        {/* Clips on timeline */}
        {clips.map((clip) => {
          const left = (clip.startTime / totalDuration) * 100;
          const width = ((clip.endTime - clip.startTime) / totalDuration) * 100;
          const isSelected = clip.id === selectedClipId;
          return (
            <div
              key={clip.id}
              className={`absolute top-1 bottom-1 rounded transition-all duration-200 ${
                isSelected
                  ? 'bg-neon/20 border border-neon/50 shadow-neon'
                  : clip.selected
                  ? 'bg-violet/15 border border-violet/30'
                  : 'bg-surface2 border border-dim/30'
              }`}
              style={{ left: `${left}%`, width: `${width}%` }}
            >
              <span className="absolute inset-0 flex items-center justify-center font-mono text-[8px] tracking-wider truncate px-1 text-paper/70">
                {clip.name}
              </span>
            </div>
          );
        })}
        {/* Playhead */}
        <div
          className="absolute top-0 bottom-0 w-0.5 bg-neon z-10"
          style={{ left: '15%' }}
        >
          <div className="absolute -top-1 -left-1 w-2.5 h-2.5 bg-neon rounded-full" />
        </div>
      </div>

      {/* Waveform placeholder */}
      <div className="h-6 bg-surface/60 rounded-md overflow-hidden relative">
        <div className="absolute inset-0 flex items-end gap-px px-1">
          {Array.from({ length: 120 }).map((_, i) => {
            const h = 15 + Math.random() * 60;
            return (
              <div
                key={i}
                className="flex-1 bg-neon/20 rounded-t-sm"
                style={{ height: `${h}%` }}
              />
            );
          })}
        </div>
      </div>
    </div>
  );
}
