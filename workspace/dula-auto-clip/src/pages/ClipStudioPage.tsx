import { useState } from 'react';
import { ArrowLeft, Download, Scissors, Palette, Music, Type, Wand2 } from 'lucide-react';
import PreviewPlayer from '@/components/PreviewPlayer';
import ClipTimeline from '@/components/ClipTimeline';
import ClipCard from '@/components/ClipCard';
import AIAdjustPanel from '@/components/AIAdjustPanel';
import GlassPanel from '@/components/GlassPanel';
import { useClipStore } from '@/stores/clipStore';
import { useNavigate } from 'react-router-dom';

const tools = [
  { id: 'clip', icon: Scissors, label: 'Clip' },
  { id: 'color', icon: Palette, label: 'Color' },
  { id: 'audio', icon: Music, label: 'Audio' },
  { id: 'subtitle', icon: Type, label: 'Sub' },
  { id: 'ai', icon: Wand2, label: 'AI' },
];

export default function ClipStudioPage() {
  const navigate = useNavigate();
  const { clips, selectedClipId, selectClip } = useClipStore();
  const [isPlaying, setIsPlaying] = useState(false);
  const [activeTool, setActiveTool] = useState<string | null>(null);

  const selectedClip = clips.find((c) => c.id === selectedClipId);

  return (
    <div className="min-h-screen bg-void flex flex-col">
      {/* Top Bar */}
      <div className="flex items-center justify-between px-6 py-3 border-b border-dim/20">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/')}
            className="p-2 rounded-md hover:bg-surface2 transition-colors"
          >
            <ArrowLeft className="w-4 h-4 text-muted" />
          </button>
          <div>
            <span className="font-grotesk font-semibold text-sm text-paper">Summer Vlog 2026</span>
            <span className="font-mono text-[9px] text-muted ml-2">8 clips selected</span>
          </div>
        </div>
        <button
          onClick={() => navigate('/export')}
          className="flex items-center gap-2 px-4 py-2 rounded-md bg-neon/10 border border-neon/30 text-neon font-grotesk text-xs font-semibold hover:bg-neon/20 transition-colors"
        >
          <Download className="w-3.5 h-3.5" />
          Export
        </button>
      </div>

      <div className="flex-1 flex">
        {/* Main Content */}
        <div className="flex-1 p-5 space-y-4 overflow-y-auto">
          {/* Preview */}
          <PreviewPlayer
            isPlaying={isPlaying}
            onTogglePlay={() => setIsPlaying(!isPlaying)}
          />

          {/* Timeline */}
          <ClipTimeline />

          {/* Tool Bar */}
          <div className="flex items-center gap-1 p-1 bg-surface rounded-lg border border-dim/20">
            {tools.map((tool) => {
              const Icon = tool.icon;
              const isActive = activeTool === tool.id;
              return (
                <button
                  key={tool.id}
                  onClick={() => setActiveTool(isActive ? null : tool.id)}
                  className={`flex-1 flex items-center justify-center gap-1.5 py-2.5 rounded-md font-grotesk text-xs font-medium transition-all duration-200 ${
                    isActive
                      ? 'bg-neon/10 text-neon border border-neon/30'
                      : 'text-muted hover:text-paper hover:bg-surface2'
                  }`}
                >
                  <Icon className="w-3.5 h-3.5" />
                  {tool.label}
                </button>
              );
            })}
          </div>

          {/* Tool Panel */}
          {activeTool && (
            <GlassPanel neon className="p-4">
              <div className="font-grotesk font-semibold text-sm text-paper mb-3">
                {activeTool === 'clip' && 'Clip Editor'}
                {activeTool === 'color' && 'Color Grading'}
                {activeTool === 'audio' && 'Audio Mixer'}
                {activeTool === 'subtitle' && 'Subtitle Editor'}
                {activeTool === 'ai' && 'AI Enhancement'}
              </div>
              <div className="grid grid-cols-4 gap-2">
                {activeTool === 'clip' && ['Split', 'Trim', 'Delete', 'Duplicate', 'Speed', 'Reverse', 'Crop', 'Rotate'].map((a) => (
                  <button key={a} className="p-2.5 rounded-md bg-surface2 border border-dim/30 font-mono text-[10px] text-muted hover:text-paper hover:border-neon/30 transition-colors">
                    {a}
                  </button>
                ))}
                {activeTool === 'color' && ['Temp', 'Tint', 'Exposure', 'Contrast', 'Saturation', 'Vibrance', 'Highlights', 'Shadows'].map((a) => (
                  <button key={a} className="p-2.5 rounded-md bg-surface2 border border-dim/30 font-mono text-[10px] text-muted hover:text-paper hover:border-violet/30 transition-colors">
                    {a}
                  </button>
                ))}
                {activeTool === 'audio' && ['Volume', 'Fade In', 'Fade Out', 'Denoise', 'EQ', 'Compress', 'Normalize', 'Duck'].map((a) => (
                  <button key={a} className="p-2.5 rounded-md bg-surface2 border border-dim/30 font-mono text-[10px] text-muted hover:text-paper hover:border-neon/30 transition-colors">
                    {a}
                  </button>
                ))}
                {activeTool === 'subtitle' && ['Auto Gen', 'Style', 'Position', 'Animate', 'Batch Edit', 'Translate', 'Timing', 'Export SRT'].map((a) => (
                  <button key={a} className="p-2.5 rounded-md bg-surface2 border border-dim/30 font-mono text-[10px] text-muted hover:text-paper hover:border-neon/30 transition-colors">
                    {a}
                  </button>
                ))}
                {activeTool === 'ai' && ['Interp 60fps', 'Upscale 4K', 'Denoise', 'Separate', 'Caption', 'Track'].map((a) => (
                  <button key={a} className="p-2.5 rounded-md bg-surface2 border border-dim/30 font-mono text-[10px] text-muted hover:text-paper hover:border-neon/30 transition-colors">
                    {a}
                  </button>
                ))}
              </div>
            </GlassPanel>
          )}
        </div>

        {/* Right Sidebar - AI Clips */}
        <div className="w-80 border-l border-dim/20 bg-surface/30 flex flex-col">
          <div className="px-4 py-3 border-b border-dim/20">
            <div className="flex items-center justify-between">
              <span className="font-grotesk font-semibold text-sm text-paper">AI Clips</span>
              <span className="font-mono text-[9px] text-neon tracking-wider">{clips.length} FOUND</span>
            </div>
          </div>

          <div className="flex-1 overflow-y-auto p-3 space-y-2">
            {clips.map((clip) => (
              <ClipCard
                key={clip.id}
                clip={clip}
                isSelected={clip.id === selectedClipId}
                isHighlighted={clip.selected}
                onSelect={selectClip}
              />
            ))}
          </div>

          {/* AI Adjust */}
          <div className="border-t border-dim/20 p-3">
            <AIAdjustPanel />
          </div>
        </div>
      </div>
    </div>
  );
}
