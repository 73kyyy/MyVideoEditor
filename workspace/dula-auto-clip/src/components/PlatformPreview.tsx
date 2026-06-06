import { Smartphone, Monitor } from 'lucide-react';

interface PlatformPreviewProps {
  platform: 'tiktok' | 'reels' | 'shorts' | 'custom';
}

const platformConfig: Record<string, { label: string; color: string; ratio: string; icon: typeof Smartphone }> = {
  tiktok: { label: 'TikTok', color: '#00F0FF', ratio: '9/16', icon: Smartphone },
  reels: { label: 'Reels', color: '#8B5CF6', ratio: '9/16', icon: Smartphone },
  shorts: { label: 'Shorts', color: '#FF6B35', ratio: '9/16', icon: Smartphone },
  custom: { label: 'Custom', color: '#6B6B76', ratio: '16/9', icon: Monitor },
};

export default function PlatformPreview({ platform }: PlatformPreviewProps) {
  const config = platformConfig[platform];
  const Icon = config.icon;
  const isVertical = platform !== 'custom';

  return (
    <div className="flex flex-col items-center gap-3">
      {/* Device Frame */}
      <div className={`relative ${isVertical ? 'w-36' : 'w-64'} ${isVertical ? 'h-64' : 'h-36'} bg-void rounded-lg border-2 overflow-hidden`}
        style={{ borderColor: `${config.color}40` }}
      >
        <div
          className="absolute inset-0 opacity-20"
          style={{
            background: `radial-gradient(circle at 40% 50%, ${config.color}40, transparent 70%)`,
          }}
        />
        <div className="absolute inset-3 border border-dashed border-dim/20 rounded" />

        {/* Platform Badge */}
        <div className="absolute top-2 left-2 px-1.5 py-0.5 rounded font-mono text-[8px] font-semibold tracking-wider"
          style={{ background: `${config.color}20`, color: config.color, border: `1px solid ${config.color}40` }}
        >
          {config.label}
        </div>

        {/* Ratio */}
        <div className="absolute bottom-2 right-2 font-mono text-[8px] text-muted">
          {config.ratio}
        </div>
      </div>

      {/* Label */}
      <div className="flex items-center gap-1.5">
        <Icon className="w-3.5 h-3.5" style={{ color: config.color }} />
        <span className="font-grotesk text-xs font-medium" style={{ color: config.color }}>
          {config.label}
        </span>
      </div>
    </div>
  );
}
