import { Play, Pause, SkipBack, SkipForward, Maximize2 } from 'lucide-react';

interface PreviewPlayerProps {
  isPlaying: boolean;
  onTogglePlay: () => void;
  currentTime?: string;
  totalTime?: string;
}

export default function PreviewPlayer({ isPlaying, onTogglePlay, currentTime = '00:08.12', totalTime = '00:24.00' }: PreviewPlayerProps) {
  return (
    <div className="glass-light rounded-xl border border-dim/30 overflow-hidden">
      {/* Video Stage */}
      <div className="relative aspect-video bg-void">
        {/* Placeholder gradient */}
        <div
          className="absolute inset-0 opacity-30"
          style={{
            background: 'radial-gradient(circle at 30% 40%, rgba(0,240,255,0.3), transparent 60%), radial-gradient(circle at 70% 60%, rgba(139,92,246,0.2), transparent 60%)',
          }}
        />
        {/* Grid overlay */}
        <div className="absolute inset-4 border border-dashed border-dim/20" />

        {/* Timecode */}
        <div className="absolute top-3 left-3 font-mono text-[10px] text-neon/80 tracking-wider">
          ● {currentTime}
        </div>
        <div className="absolute top-3 right-3 font-mono text-[10px] text-paper/50 tracking-wider">
          1920×1080
        </div>

        {/* AI Badge */}
        <div className="absolute bottom-3 left-3 px-2 py-1 rounded bg-neon/20 border border-neon/30 font-mono text-[9px] text-neon font-semibold tracking-wider">
          AI READY
        </div>
        <div className="absolute bottom-3 right-3 flex items-center gap-1 font-mono text-[9px] text-paper/50">
          <span className="w-1.5 h-1.5 bg-neon rounded-full animate-pulse2" />
          LIVE
        </div>

        {/* Play Button */}
        <div className="absolute inset-0 flex items-center justify-center">
          <button
            onClick={onTogglePlay}
            className="w-16 h-16 rounded-full bg-paper/5 backdrop-blur-sm border border-paper/20 flex items-center justify-center hover:bg-neon/10 hover:border-neon/40 transition-all duration-200 active:scale-90"
          >
            {isPlaying ? (
              <Pause className="w-7 h-7 text-paper" />
            ) : (
              <Play className="w-7 h-7 text-paper ml-1" />
            )}
          </button>
        </div>

        {/* Fullscreen */}
        <button className="absolute top-3 right-3 mt-6 p-1.5 rounded bg-void/50 hover:bg-surface2/50 transition-colors">
          <Maximize2 className="w-3.5 h-3.5 text-paper/50" />
        </button>
      </div>

      {/* Controls */}
      <div className="px-4 py-3 flex items-center justify-between border-t border-dim/20">
        <div className="flex items-center gap-2">
          <button className="p-1.5 rounded hover:bg-surface2 transition-colors">
            <SkipBack className="w-4 h-4 text-muted" />
          </button>
          <button
            onClick={onTogglePlay}
            className="p-2 rounded-md bg-neon/10 border border-neon/30 hover:bg-neon/20 transition-colors"
          >
            {isPlaying ? <Pause className="w-4 h-4 text-neon" /> : <Play className="w-4 h-4 text-neon ml-0.5" />}
          </button>
          <button className="p-1.5 rounded hover:bg-surface2 transition-colors">
            <SkipForward className="w-4 h-4 text-muted" />
          </button>
        </div>
        <div className="font-mono text-xs text-muted">
          <span className="text-paper">{currentTime}</span>
          <span className="mx-1">/</span>
          <span>{totalTime}</span>
        </div>
      </div>
    </div>
  );
}
