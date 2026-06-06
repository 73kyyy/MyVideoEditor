import { SlidersHorizontal } from 'lucide-react';
import GlassPanel from './GlassPanel';
import { useAIStore } from '@/stores/aiStore';

export default function AIAdjustPanel() {
  const { analysis, setSensitivity, setEmotionWeight, setRhythmWeight } = useAIStore();

  const sliders = [
    { label: 'SENSITIVITY', value: analysis.sensitivity, onChange: setSensitivity, color: '#00F0FF' },
    { label: 'EMOTION WT', value: analysis.emotionWeight, onChange: setEmotionWeight, color: '#FF6B35' },
    { label: 'RHYTHM WT', value: analysis.rhythmWeight, onChange: setRhythmWeight, color: '#8B5CF6' },
  ];

  return (
    <GlassPanel violet className="p-5">
      <div className="flex items-center gap-2 mb-4">
        <SlidersHorizontal className="w-4 h-4 text-violet" />
        <span className="font-grotesk font-semibold text-sm text-paper">AI Parameters</span>
      </div>

      <div className="space-y-4">
        {sliders.map((slider) => (
          <div key={slider.label}>
            <div className="flex items-center justify-between mb-1.5">
              <span className="font-mono text-[10px] tracking-wider text-muted">{slider.label}</span>
              <span className="font-mono text-xs font-semibold text-paper">{slider.value}%</span>
            </div>
            <div className="relative h-2 bg-surface3 rounded-full cursor-pointer group">
              <div
                className="absolute left-0 top-0 bottom-0 rounded-full transition-all duration-200"
                style={{ width: `${slider.value}%`, background: slider.color, opacity: 0.6 }}
              />
              <input
                type="range"
                min={0}
                max={100}
                value={slider.value}
                onChange={(e) => slider.onChange(Number(e.target.value))}
                className="absolute inset-0 w-full opacity-0 cursor-pointer"
              />
              <div
                className="absolute top-1/2 -translate-y-1/2 w-3.5 h-3.5 rounded-full border-2 bg-void transition-all duration-200 group-hover:scale-110"
                style={{ left: `calc(${slider.value}% - 7px)`, borderColor: slider.color }}
              />
            </div>
          </div>
        ))}
      </div>

      {/* Scene Filter */}
      <div className="mt-5">
        <span className="font-mono text-[10px] tracking-wider text-muted block mb-2">SCENE FILTER</span>
        <div className="flex flex-wrap gap-1.5">
          {['Indoor', 'Outdoor', 'Close-up', 'Wide', 'Action', 'Static'].map((filter) => (
            <button
              key={filter}
              className="px-2.5 py-1 rounded-md bg-surface3 border border-dim/30 font-mono text-[10px] text-muted hover:text-paper hover:border-violet/30 transition-colors"
            >
              {filter}
            </button>
          ))}
        </div>
      </div>
    </GlassPanel>
  );
}
