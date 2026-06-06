import StatusBar from '@/components/StatusBar';
import BottomNav from '@/components/BottomNav';
import { useSettingsStore } from '@/stores/settingsStore';

interface SettingSection {
  title: string;
  items: {
    icon: string;
    label: string;
    value?: string;
    toggle?: boolean;
    toggleOn?: boolean;
    arrow?: boolean;
  }[];
}

const sections: SettingSection[] = [
  {
    title: 'Project',
    items: [
      { icon: '⊞', label: 'Default Quality', value: '1080P', arrow: true },
      { icon: '▤', label: 'Export Format', value: 'MP4 · H.265', arrow: true },
      { icon: '◉', label: 'Watermark', toggle: true, toggleOn: false },
      { icon: '▥', label: 'Storage', value: 'INTERNAL', arrow: true },
    ],
  },
  {
    title: 'Performance',
    items: [
      { icon: '▰', label: 'GPU Acceleration', toggle: true, toggleOn: true },
      { icon: '⌘', label: 'AI Inference', value: 'FP16', arrow: true },
      { icon: '⌫', label: 'Cache', value: '1.2 GB', arrow: true },
    ],
  },
  {
    title: 'Appearance',
    items: [
      { icon: '◐', label: 'Dark Mode', toggle: true, toggleOn: false },
      { icon: '▦', label: 'Typeface', value: 'EDITORIAL', arrow: true },
    ],
  },
  {
    title: 'About',
    items: [
      { icon: 'ⓘ', label: 'Version', value: '1.0.0' },
      { icon: '✉', label: 'Feedback', arrow: true },
    ],
  },
];

export default function SettingsPage() {
  const { settings, updateSetting } = useSettingsStore();

  return (
    <div className="h-full flex flex-col">
      <StatusBar />

      {/* Header */}
      <div className="px-6 pt-5 pb-3.5 border-b-1.5 border-ink flex-shrink-0">
        <div className="font-mono text-[10px] tracking-[2px] uppercase text-ink-3 mb-1">
          Configuration / 003
        </div>
        <h1 className="font-fraunces text-[42px] font-black tracking-[-1.5px] leading-none">
          SET<br />
          <em className="font-light text-accent">TINGS</em>.
        </h1>
      </div>

      {/* Body */}
      <div className="flex-1 overflow-y-auto scrollbar-none pb-20">
        {/* Profile */}
        <div className="px-6 py-5 border-b-1.5 border-ink grid grid-cols-[auto_1fr_auto] gap-3.5 items-center">
          <div className="w-14 h-14 bg-ink text-paper flex items-center justify-center font-fraunces text-[32px] font-black italic">
            N
          </div>
          <div>
            <div className="font-fraunces text-lg font-bold">NexClip User</div>
            <div className="font-mono text-[10px] text-ink-3 tracking-wider">
              USER@NEXCLIP.APP
            </div>
          </div>
          <button className="w-8 h-8 border-1.5 border-ink bg-transparent cursor-pointer font-bold flex items-center justify-center">
            ✎
          </button>
        </div>

        {/* Sections */}
        {sections.map((section) => (
          <div key={section.title}>
            <div className="px-6 pt-5 pb-1.5 font-mono text-[10px] tracking-[2px] font-bold text-ink-3 uppercase">
              {section.title}
            </div>
            <div className="border-t-1.5 border-b-1.5 border-ink">
              {section.items.map((item, i) => (
                <button
                  key={item.label}
                  onClick={() => {
                    if (item.toggle) {
                      if (item.label === 'Watermark') updateSetting('watermark', !settings.watermark);
                      if (item.label === 'GPU Acceleration') updateSetting('gpuAcceleration', !settings.gpuAcceleration);
                      if (item.label === 'Dark Mode') updateSetting('darkMode', !settings.darkMode);
                    }
                  }}
                  className="grid grid-cols-[auto_1fr_auto_auto] gap-3.5 items-center px-6 py-4 w-full text-left font-body text-ink cursor-pointer active:bg-paper-2 border-b border-ink last:border-b-0"
                >
                  <div className="w-8 h-8 bg-ink text-paper flex items-center justify-center text-sm font-light">
                    {item.icon}
                  </div>
                  <span className="text-sm font-semibold">{item.label}</span>
                  {item.value && (
                    <span className="font-mono text-[10px] text-ink-3 tracking-wide">
                      {item.value}
                    </span>
                  )}
                  {item.toggle && (
                    <div
                      className={`w-10 h-[22px] border-1.5 border-ink relative transition-colors ${
                        (item.label === 'Watermark' && settings.watermark) ||
                        (item.label === 'GPU Acceleration' && settings.gpuAcceleration) ||
                        (item.label === 'Dark Mode' && settings.darkMode)
                          ? 'bg-accent border-accent'
                          : 'bg-paper-2'
                      }`}
                    >
                      <div
                        className={`absolute top-1/2 w-3.5 h-3.5 transition-transform -translate-y-1/2 ${
                          (item.label === 'Watermark' && settings.watermark) ||
                          (item.label === 'GPU Acceleration' && settings.gpuAcceleration) ||
                          (item.label === 'Dark Mode' && settings.darkMode)
                            ? 'bg-paper translate-x-4'
                            : 'bg-ink translate-x-0.5'
                        }`}
                      />
                    </div>
                  )}
                  {item.arrow && !item.toggle && (
                    <span className="text-ink-3 font-bold text-base">→</span>
                  )}
                  {!item.value && !item.toggle && !item.arrow && <span />}
                </button>
              ))}
            </div>
          </div>
        ))}

        {/* Version */}
        <div className="px-6 py-6 text-center font-mono text-[10px] tracking-[2px] text-ink-3 uppercase">
          NEXCLIP <span className="text-accent font-bold">v1.0.0</span> · BUILD 2026.06
        </div>
      </div>

      <BottomNav />
    </div>
  );
}
