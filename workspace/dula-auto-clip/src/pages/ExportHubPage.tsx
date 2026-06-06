import { useState } from 'react';
import { ArrowLeft, Download, CheckSquare, Square, Settings2 } from 'lucide-react';
import PlatformPreview from '@/components/PlatformPreview';
import ExportQueue from '@/components/ExportQueue';
import GlassPanel from '@/components/GlassPanel';
import { useClipStore } from '@/stores/clipStore';
import { useExportStore } from '@/stores/exportStore';
import { useNavigate } from 'react-router-dom';

const qualityOptions = ['720p', '1080p', '4k'] as const;
const formatOptions = ['mp4', 'mov'] as const;

export default function ExportHubPage() {
  const navigate = useNavigate();
  const { clips } = useClipStore();
  const { tasks } = useExportStore();
  const [selectedPlatform, setSelectedPlatform] = useState<'tiktok' | 'reels' | 'shorts' | 'custom'>('tiktok');
  const [selectedQuality, setSelectedQuality] = useState('1080p');
  const [selectedFormat, setSelectedFormat] = useState('mp4');
  const [selectedClips, setSelectedClips] = useState<Set<string>>(
    new Set(clips.filter((c) => c.selected).map((c) => c.id))
  );

  const toggleClip = (id: string) => {
    setSelectedClips((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const platforms: Array<'tiktok' | 'reels' | 'shorts' | 'custom'> = ['tiktok', 'reels', 'shorts', 'custom'];

  return (
    <div className="min-h-screen bg-void">
      {/* Top Bar */}
      <div className="flex items-center justify-between px-6 py-3 border-b border-dim/20">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate('/studio')} className="p-2 rounded-md hover:bg-surface2 transition-colors">
            <ArrowLeft className="w-4 h-4 text-muted" />
          </button>
          <span className="font-grotesk font-semibold text-sm text-paper">Export Hub</span>
          <span className="font-mono text-[9px] text-muted">{selectedClips.size} clips selected</span>
        </div>
        <button className="flex items-center gap-2 px-5 py-2.5 rounded-lg bg-gradient-to-r from-neon to-violet text-void font-grotesk text-sm font-bold hover:opacity-90 transition-opacity">
          <Download className="w-4 h-4" />
          Export All
        </button>
      </div>

      <div className="px-6 py-6 grid grid-cols-12 gap-6">
        {/* Left - Platform Preview */}
        <div className="col-span-5 space-y-5">
          <GlassPanel neon className="p-6">
            <h3 className="font-grotesk font-semibold text-base text-paper mb-5">Platform Preview</h3>

            {/* Platform Tabs */}
            <div className="flex gap-1 mb-6 p-1 bg-surface2 rounded-lg">
              {platforms.map((p) => (
                <button
                  key={p}
                  onClick={() => setSelectedPlatform(p)}
                  className={`flex-1 py-2 rounded-md font-grotesk text-xs font-medium capitalize transition-all duration-200 ${
                    selectedPlatform === p
                      ? 'bg-neon/10 text-neon border border-neon/30'
                      : 'text-muted hover:text-paper'
                  }`}
                >
                  {p}
                </button>
              ))}
            </div>

            {/* Preview */}
            <div className="flex justify-center">
              <PlatformPreview platform={selectedPlatform} />
            </div>
          </GlassPanel>

          {/* Export Settings */}
          <GlassPanel className="p-5">
            <div className="flex items-center gap-2 mb-4">
              <Settings2 className="w-4 h-4 text-violet" />
              <span className="font-grotesk font-semibold text-sm text-paper">Export Settings</span>
            </div>

            <div className="space-y-4">
              {/* Quality */}
              <div>
                <span className="font-mono text-[10px] text-muted tracking-wider uppercase block mb-2">Quality</span>
                <div className="flex gap-1.5">
                  {qualityOptions.map((q) => (
                    <button
                      key={q}
                      onClick={() => setSelectedQuality(q)}
                      className={`flex-1 py-2 rounded-md font-mono text-xs font-medium transition-all duration-200 ${
                        selectedQuality === q
                          ? 'bg-neon/10 text-neon border border-neon/30'
                          : 'bg-surface2 text-muted border border-dim/20 hover:text-paper'
                      }`}
                    >
                      {q}
                    </button>
                  ))}
                </div>
              </div>

              {/* Format */}
              <div>
                <span className="font-mono text-[10px] text-muted tracking-wider uppercase block mb-2">Format</span>
                <div className="flex gap-1.5">
                  {formatOptions.map((f) => (
                    <button
                      key={f}
                      onClick={() => setSelectedFormat(f)}
                      className={`flex-1 py-2 rounded-md font-mono text-xs font-medium transition-all duration-200 ${
                        selectedFormat === f
                          ? 'bg-violet/10 text-violet border border-violet/30'
                          : 'bg-surface2 text-muted border border-dim/20 hover:text-paper'
                      }`}
                    >
                      {f.toUpperCase()}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </GlassPanel>
        </div>

        {/* Right - Clip Selection & Queue */}
        <div className="col-span-7 space-y-5">
          {/* Clip Selection */}
          <GlassPanel className="p-5">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-grotesk font-semibold text-base text-paper">Select Clips</h3>
              <button
                onClick={() => {
                  if (selectedClips.size === clips.length) setSelectedClips(new Set());
                  else setSelectedClips(new Set(clips.map((c) => c.id)));
                }}
                className="font-mono text-[10px] text-neon hover:text-neon2 transition-colors"
              >
                {selectedClips.size === clips.length ? 'DESELECT ALL' : 'SELECT ALL'}
              </button>
            </div>

            <div className="space-y-1.5 max-h-64 overflow-y-auto">
              {clips.map((clip) => {
                const isSelected = selectedClips.has(clip.id);
                return (
                  <div
                    key={clip.id}
                    onClick={() => toggleClip(clip.id)}
                    className={`flex items-center gap-3 p-2.5 rounded-lg cursor-pointer transition-all duration-200 ${
                      isSelected ? 'bg-neon/5 border border-neon/20' : 'hover:bg-surface2 border border-transparent'
                    }`}
                  >
                    {isSelected ? (
                      <CheckSquare className="w-4 h-4 text-neon flex-shrink-0" />
                    ) : (
                      <Square className="w-4 h-4 text-dim flex-shrink-0" />
                    )}
                    <div className="flex-1 min-w-0">
                      <span className={`font-grotesk text-xs font-medium ${isSelected ? 'text-neon' : 'text-paper'}`}>
                        {clip.name}
                      </span>
                    </div>
                    <span className="font-mono text-[9px] text-muted">
                      {clip.startTime}s – {clip.endTime}s
                    </span>
                    <span className={`font-mono text-[10px] font-semibold ${
                      clip.score >= 90 ? 'text-neon' : clip.score >= 80 ? 'text-violet' : 'text-heat'
                    }`}>
                      {clip.score}
                    </span>
                  </div>
                );
              })}
            </div>
          </GlassPanel>

          {/* Export Queue */}
          <GlassPanel className="p-5">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-grotesk font-semibold text-base text-paper">Export Queue</h3>
              <span className="font-mono text-[9px] text-muted tracking-wider">{tasks.length} TASKS</span>
            </div>
            <ExportQueue tasks={tasks} />
          </GlassPanel>
        </div>
      </div>
    </div>
  );
}
