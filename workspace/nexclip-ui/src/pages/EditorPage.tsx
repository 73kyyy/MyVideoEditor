import { useNavigate } from 'react-router-dom';
import StatusBar from '@/components/StatusBar';
import BottomNav from '@/components/BottomNav';
import ColorSlider from '@/components/ColorSlider';
import SpeedCurveCanvas from '@/components/SpeedCurveCanvas';
import ScopeCanvas from '@/components/ScopeCanvas';
import { useEditorStore } from '@/stores/editorStore';
import type { ColorGradingTab, SpeedCurveType } from '@/types';

const tools = [
  { id: 'clip', icon: '✂', label: 'Edit' },
  { id: 'speed', icon: '↯', label: 'Speed' },
  { id: 'color', icon: '◐', label: 'Color' },
  { id: 'audio', icon: '♪', label: 'Audio' },
  { id: 'fx', icon: '✦', label: 'FX' },
  { id: 'ai', icon: '◎', label: 'AI' },
];

const cgTabs: { key: ColorGradingTab; label: string }[] = [
  { key: 'basic', label: 'Basic' },
  { key: 'curve', label: 'Curves' },
  { key: 'wheel', label: 'Wheels' },
  { key: 'hsl', label: 'HSL' },
];

const speedPresets: { key: SpeedCurveType; label: string }[] = [
  { key: 'linear', label: 'Linear' },
  { key: 'in', label: 'Ease In' },
  { key: 'out', label: 'Ease Out' },
  { key: 'both', label: 'In / Out' },
  { key: 'bounce', label: 'Bounce' },
  { key: 'custom', label: 'Custom' },
];

const fxItems = [
  { icon: '⟶', label: 'Trans' },
  { icon: '✦', label: 'Light' },
  { icon: '≈', label: 'Blur' },
  { icon: '▣', label: 'Glitch' },
  { icon: '◉', label: 'Style' },
  { icon: '◈', label: 'Morph' },
];

const editActions = [
  { icon: '✂', label: 'Split' },
  { icon: '⌫', label: 'Delete' },
  { icon: '↔', label: 'Crop' },
  { icon: '↻', label: 'Rotate' },
  { icon: '⊞', label: 'PiP' },
  { icon: '⏎', label: 'Reverse' },
];

export default function EditorPage() {
  const navigate = useNavigate();
  const {
    currentProject,
    currentTime,
    isPlaying,
    activePanel,
    colorGradingTab,
    speedCurveType,
    aiFeatures,
    togglePlay,
    openPanel,
    closePanel,
    setColorGradingTab,
    setSpeedCurveType,
    toggleAIFeature,
  } = useEditorStore();

  const projectName = currentProject?.name || 'Untitled';

  const handleToolClick = (toolId: string) => {
    if (activePanel === toolId) {
      closePanel();
    } else {
      openPanel(toolId);
    }
  };

  return (
    <div className="h-full flex flex-col relative">
      <StatusBar />

      {/* Top Bar */}
      <div className="flex justify-between items-center px-5 py-3 border-b-1.5 border-ink flex-shrink-0">
        <button
          onClick={() => navigate('/')}
          className="w-9 h-9 border-1.5 border-ink bg-transparent cursor-pointer font-body text-base font-bold flex items-center justify-center"
        >
          ←
        </button>
        <div className="font-fraunces text-base font-bold italic">{projectName}</div>
        <button className="h-8 px-4 bg-ink text-paper border-none font-body text-[11px] font-bold tracking-[1.5px] cursor-pointer uppercase active:bg-accent">
          Export
        </button>
      </div>

      {/* Preview Stage */}
      <div className="mx-3.5 mt-3.5 border-1.5 border-ink aspect-video relative bg-ink flex-shrink-0">
        <div
          className="absolute inset-2 border border-dashed border-white/15 pointer-events-none"
        />
        <span className="absolute top-2 left-2 font-mono text-[9px] text-accent-2 font-bold tracking-wider z-10">
          ● REC 00:{currentTime.toFixed(2)}
        </span>
        <span className="absolute top-2 right-2 font-mono text-[9px] text-paper font-medium tracking-wider z-10">
          1920×1080 · 30FPS
        </span>
        <span className="absolute bottom-2 left-2 bg-accent text-paper font-mono text-[9px] font-bold px-2 py-1 tracking-wider z-10">
          AI READY
        </span>
        <span className="absolute bottom-2 right-2 flex items-center gap-1 font-mono text-[9px] text-paper z-10">
          <span className="w-1.5 h-1.5 bg-accent rounded-full animate-pulse" />
          LIVE PREVIEW
        </span>
        <div className="absolute inset-0 flex items-center justify-center cursor-pointer z-20">
          <div className="w-16 h-16 border-2 border-paper flex items-center justify-center text-[22px] text-paper bg-white/5 backdrop-blur-sm font-light">
            {isPlaying ? '⏸' : '▶'}
          </div>
        </div>
        <div
          className="absolute inset-0 opacity-15"
          style={{
            background:
              'radial-gradient(circle at 20% 30%, rgba(255,61,0,0.4), transparent 50%), radial-gradient(circle at 80% 70%, rgba(255,214,0,0.2), transparent 50%)',
          }}
        />
      </div>

      {/* Meta */}
      <div className="flex justify-between px-5 font-mono text-[11px] font-medium my-2 flex-shrink-0">
        <span>FRAMES 247/720</span>
        <span className="font-bold">2.4 GB</span>
      </div>

      {/* Progress */}
      <div className="h-1.5 mx-5 bg-paper-2 border border-ink relative flex-shrink-0">
        <div className="absolute left-0 top-0 bottom-0 bg-ink w-[35%]">
          <div className="absolute right-[-3px] top-1/2 -translate-y-1/2 w-1.5 h-3.5 bg-accent" />
        </div>
      </div>
      <div className="flex justify-between px-5 pt-1.5 font-mono text-[10px] text-ink-3 flex-shrink-0">
        <span>00:08.12</span>
        <span>00:24.00</span>
      </div>

      {/* Controls */}
      <div className="flex justify-center items-center gap-3 px-5 py-3.5 flex-shrink-0">
        <button className="w-10 h-10 bg-transparent border-1.5 border-ink flex items-center justify-center cursor-pointer text-base text-ink font-body">
          ⏮
        </button>
        <button className="w-10 h-10 bg-transparent border-1.5 border-ink flex items-center justify-center cursor-pointer text-base text-ink font-body">
          ⏪
        </button>
        <button
          onClick={togglePlay}
          className="w-14 h-14 bg-ink text-paper flex items-center justify-center cursor-pointer text-[22px] active:bg-accent"
        >
          {isPlaying ? '⏸' : '▶'}
        </button>
        <button className="w-10 h-10 bg-transparent border-1.5 border-ink flex items-center justify-center cursor-pointer text-base text-ink font-body">
          ⏩
        </button>
        <button className="w-10 h-10 bg-transparent border-1.5 border-ink flex items-center justify-center cursor-pointer text-base text-ink font-body">
          ⏭
        </button>
      </div>

      {/* Timeline */}
      <div className="border-t-1.5 border-b-1.5 border-ink flex-shrink-0 bg-paper">
        <div className="flex justify-between px-5 py-2 font-mono text-[9px] tracking-wider text-ink-3 border-b border-ink">
          <span>TIMELINE / 24.0S</span>
          <span className="text-ink font-bold">ZOOM 100%</span>
        </div>
        <div className="py-2.5 relative pl-11">
          <div className="absolute left-5 top-1/2 -translate-y-1/2 font-mono text-[9px] font-bold text-ink-3 tracking-[2px]" style={{ writingMode: 'vertical-rl' }}>
            TRACKS
          </div>
          <div className="h-3.5 bg-ink flex items-center justify-end pr-1.5 font-mono text-[8px] text-accent-2 font-bold tracking-wider mb-1">
            00:08.12
          </div>
          {[
            { label: 'V1', clips: [{ name: 'MAIN', width: '60%', solid: true }, { name: 'PiP', width: '25%', solid: false }] },
            { label: 'V2', clips: [{ name: 'TITLE', width: '35%', solid: false }] },
            { label: 'A1', clips: [{ name: 'MUSIC', width: '55%', solid: true, audio: true }] },
            { label: 'A2', clips: [{ name: 'VO', width: '25%', solid: false }] },
          ].map((track) => (
            <div key={track.label} className="h-[26px] my-1.5 mr-5 flex items-center relative">
              <span className="absolute left-[-32px] top-1/2 -translate-y-1/2 font-mono text-[8px] text-ink-3 font-bold">
                {track.label}
              </span>
              {track.clips.map((clip, i) => (
                <div
                  key={i}
                  className={`h-full flex items-center px-2 font-mono text-[9px] font-bold tracking-wider cursor-pointer border-1.5 border-ink mr-1.5 ${
                    clip.solid
                      ? clip.audio
                        ? 'bg-green text-paper border-green'
                        : 'bg-ink text-paper'
                      : 'bg-transparent text-ink border-dashed'
                  }`}
                  style={{ width: clip.width }}
                >
                  {clip.audio && <span className="mr-1 text-accent-2">♫</span>}
                  {clip.name}
                </div>
              ))}
            </div>
          ))}
          <div className="absolute top-0 bottom-0 left-[35%] w-0.5 bg-accent z-10">
            <div className="absolute -top-0.5 -left-[5px] border-[6px] border-accent border-b-transparent border-l-transparent border-r-transparent w-0 h-0" />
          </div>
        </div>
      </div>

      {/* Toolbar */}
      <div className="grid grid-cols-6 border-t-1.5 border-ink flex-shrink-0 bg-paper">
        {tools.map((tool) => (
          <button
            key={tool.id}
            onClick={() => handleToolClick(tool.id)}
            className={`py-3 px-1 pb-4 border-none bg-transparent font-body cursor-pointer border-r border-ink last:border-r-0 relative ${
              activePanel === tool.id ? 'bg-ink text-paper' : 'text-ink'
            }`}
          >
            {activePanel === tool.id && (
              <span className="absolute -top-[1.5px] left-0 right-0 h-[3px] bg-accent" />
            )}
            <span className="block text-lg mb-1 font-light">{tool.icon}</span>
            <span className="block text-[9px] font-bold tracking-[1.5px] uppercase">
              {tool.label}
            </span>
          </button>
        ))}
      </div>

      {/* Panels */}
      <div
        className={`absolute bottom-0 left-0 right-0 bg-paper border-t-[3px] border-ink z-20 transition-transform duration-300 ease-[cubic-bezier(0.4,0,0.2,1)] max-h-[60%] flex flex-col ${
          activePanel ? 'translate-y-0' : 'translate-y-full'
        }`}
      >
        {activePanel && (
          <>
            {/* Panel Header */}
            <div className="flex justify-between items-center px-5 py-3.5 border-b-1.5 border-ink flex-shrink-0">
              <h3 className="font-fraunces text-xl font-bold italic">
                {activePanel === 'clip' && 'Edit'}
                {activePanel === 'speed' && 'Speed Curve'}
                {activePanel === 'color' && 'Color'}
                {activePanel === 'audio' && 'Audio'}
                {activePanel === 'fx' && 'Effects'}
                {activePanel === 'ai' && 'AI Tools'}
                <span className="font-mono text-[9px] text-ink-3 ml-2 not-italic tracking-wider align-middle">
                  {activePanel === 'clip' && '04 ACTIONS'}
                  {activePanel === 'speed' && '01.0x'}
                  {activePanel === 'color' && 'LUMETRI'}
                  {activePanel === 'audio' && 'MIX'}
                  {activePanel === 'fx' && '06'}
                  {activePanel === 'ai' && '06'}
                </span>
              </h3>
              <button
                onClick={closePanel}
                className="w-[30px] h-[30px] border-1.5 border-ink bg-transparent cursor-pointer font-bold text-sm flex items-center justify-center"
              >
                ×
              </button>
            </div>

            {/* Panel Body */}
            <div className="flex-1 overflow-y-auto p-5 scrollbar-none">
              {/* Clip Panel */}
              {activePanel === 'clip' && (
                <div className="grid grid-cols-3 gap-2.5">
                  {editActions.map((a) => (
                    <div
                      key={a.label}
                      className="aspect-square border-1.5 border-ink flex flex-col items-center justify-center cursor-pointer active:bg-ink active:text-paper"
                    >
                      <span className="text-[28px] mb-1.5 font-light">{a.icon}</span>
                      <span className="font-mono text-[9px] font-bold tracking-[1.5px] uppercase">
                        {a.label}
                      </span>
                    </div>
                  ))}
                </div>
              )}

              {/* Speed Panel */}
              {activePanel === 'speed' && (
                <>
                  <SpeedCurveCanvas type={speedCurveType} />
                  <div className="grid grid-cols-3 gap-0 border-1.5 border-ink mb-3.5">
                    {speedPresets.map((p) => (
                      <button
                        key={p.key}
                        onClick={() => setSpeedCurveType(p.key)}
                        className={`py-2.5 border-none font-body text-[10px] font-bold tracking-wider cursor-pointer uppercase border-r border-ink last:border-r-0 ${
                          speedCurveType === p.key
                            ? 'bg-ink text-paper'
                            : 'bg-paper text-ink'
                        }`}
                      >
                        {p.label}
                      </button>
                    ))}
                  </div>
                  <ColorSlider label="SPEED" value="1.0x" fillWidth="50%" fillColor="#FF3D00" />
                </>
              )}

              {/* Color Panel */}
              {activePanel === 'color' && (
                <>
                  <div className="flex gap-0 mb-5 border-1.5 border-ink">
                    {cgTabs.map((t) => (
                      <button
                        key={t.key}
                        onClick={() => setColorGradingTab(t.key)}
                        className={`flex-1 py-2.5 border-none font-body text-[10px] font-bold tracking-[1.5px] cursor-pointer uppercase border-r border-ink last:border-r-0 ${
                          colorGradingTab === t.key
                            ? 'bg-ink text-paper'
                            : 'bg-paper text-ink'
                        }`}
                      >
                        {t.label}
                      </button>
                    ))}
                  </div>

                  {colorGradingTab === 'basic' && (
                    <>
                      <ColorSlider label="TEMP" value="+12" fillWidth="60%" fillColor="#FF3D00" />
                      <ColorSlider label="TINT" value="−5" fillWidth="30%" fillColor="#1A6B3F" />
                      <ColorSlider label="EXPO" value="+0.3" fillWidth="45%" fillColor="#FFD600" />
                      <ColorSlider label="CONTR" value="+15" fillWidth="55%" fillColor="#FF3D00" />
                      <ColorSlider label="SAT" value="+20" fillWidth="70%" fillColor="#1A6B3F" />
                      <ColorSlider label="VIBE" value="+10" fillWidth="50%" fillColor="#FFD600" />
                    </>
                  )}

                  {colorGradingTab === 'curve' && (
                    <>
                      <svg viewBox="0 0 350 140" className="w-full border-1.5 border-ink bg-paper-2 mb-3.5">
                        {[35, 70, 105].map((y) => (
                          <line key={y} x1="0" y1={y} x2="350" y2={y} stroke="#0A0A0A" strokeWidth="0.5" strokeDasharray="2 3" />
                        ))}
                        {[87, 175, 262].map((x) => (
                          <line key={x} x1={x} y1="0" x2={x} y2="140" stroke="#0A0A0A" strokeWidth="0.5" strokeDasharray="2 3" />
                        ))}
                        <path d="M0,140 C70,140 105,90 175,70 C245,50 280,25 350,0" stroke="#FF3D00" strokeWidth="2.5" fill="none" />
                        {[[0, 140], [105, 90], [175, 70], [245, 50], [350, 0]].map(([cx, cy], i) => (
                          <circle key={i} cx={cx} cy={cy} r="5" fill="#F2EFE9" stroke={i === 2 ? '#FF3D00' : '#0A0A0A'} strokeWidth="2" />
                        ))}
                      </svg>
                      <div className="grid grid-cols-4 gap-0 border-1.5 border-ink">
                        {['RGB', 'RED', 'GRN', 'BLU'].map((c, i) => (
                          <button
                            key={c}
                            className={`py-1.5 border-none font-body text-[11px] font-bold tracking-wider cursor-pointer border-r border-ink last:border-r-0 ${
                              i === 0 ? 'bg-accent text-paper' : 'bg-paper text-ink'
                            }`}
                          >
                            {c}
                          </button>
                        ))}
                      </div>
                    </>
                  )}

                  {colorGradingTab === 'wheel' && (
                    <>
                      <div className="grid grid-cols-3 gap-2.5 mb-3.5">
                        {[
                          { label: 'SHADOW', top: '25%', left: '65%' },
                          { label: 'MID', top: '40%', left: '50%' },
                          { label: 'HIGH', top: '30%', left: '60%' },
                        ].map((w) => (
                          <div key={w.label} className="text-center">
                            <div
                              className="aspect-square border-1.5 border-ink rounded-full relative mx-auto"
                              style={{
                                background: 'conic-gradient(from 0deg, #FF3D00, #FFD600, #1A6B3F, #0EA5E9, #7C3AED, #FF3D00)',
                              }}
                            >
                              <div className="absolute inset-[30%] bg-paper rounded-full border-1.5 border-ink" />
                              <div
                                className="absolute w-2.5 h-2.5 bg-ink border-2 border-paper rounded-full z-10"
                                style={{ top: w.top, left: w.left }}
                              />
                            </div>
                            <div className="font-mono text-[9px] tracking-[1.5px] text-center mt-1.5 font-bold">
                              {w.label}
                            </div>
                          </div>
                        ))}
                      </div>
                      <div className="font-mono text-[9px] tracking-[1.5px] font-bold mb-2">
                        SCOPES
                      </div>
                      <div className="grid grid-cols-2 gap-2">
                        <div className="border-1.5 border-ink overflow-hidden">
                          <ScopeCanvas type="waveform" />
                          <div className="font-mono text-[9px] text-center py-1 text-ink-3 font-bold tracking-wider">
                            WAVEFORM
                          </div>
                        </div>
                        <div className="border-1.5 border-ink overflow-hidden">
                          <ScopeCanvas type="vector" />
                          <div className="font-mono text-[9px] text-center py-1 text-ink-3 font-bold tracking-wider">
                            VECTOR
                          </div>
                        </div>
                        <div className="border-1.5 border-ink overflow-hidden">
                          <ScopeCanvas type="parade" />
                          <div className="font-mono text-[9px] text-center py-1 text-ink-3 font-bold tracking-wider">
                            PARADE
                          </div>
                        </div>
                        <div className="border-1.5 border-ink overflow-hidden">
                          <ScopeCanvas type="histogram" />
                          <div className="font-mono text-[9px] text-center py-1 text-ink-3 font-bold tracking-wider">
                            HISTOGRAM
                          </div>
                        </div>
                      </div>
                    </>
                  )}

                  {colorGradingTab === 'hsl' && (
                    <>
                      <div className="font-mono text-[9px] tracking-[1.5px] font-bold mb-2.5">
                        HUE
                      </div>
                      {[
                        { label: 'RED', color: '#DC2626', val: '+0', left: '30%' },
                        { label: 'ORNG', color: '#EA580C', val: '+5', left: '40%' },
                        { label: 'YEL', color: '#CA8A04', val: '0', left: '50%' },
                        { label: 'GRN', color: '#16A34A', val: '−3', left: '45%' },
                        { label: 'BLU', color: '#0284C7', val: '0', left: '50%' },
                      ].map((row) => (
                        <div key={row.label} className="grid grid-cols-[60px_1fr_36px] gap-2 items-center mb-2">
                          <span className="font-mono text-[10px] font-bold" style={{ color: row.color }}>
                            {row.label}
                          </span>
                          <div className="h-1 bg-paper-2 border border-ink relative">
                            <div
                              className="absolute top-1/2 w-2.5 h-2.5 bg-paper border-2 border-ink -translate-y-1/2 -translate-x-1/2"
                              style={{ left: row.left }}
                            />
                          </div>
                          <span className="font-mono text-[10px] font-bold text-right">{row.val}</span>
                        </div>
                      ))}
                      <div className="font-mono text-[9px] tracking-[1.5px] font-bold mt-3.5 mb-2.5">
                        SATURATION
                      </div>
                      {[
                        { label: 'RED', color: '#DC2626', val: '+10', left: '65%' },
                        { label: 'GRN', color: '#16A34A', val: '−5', left: '40%' },
                      ].map((row) => (
                        <div key={row.label} className="grid grid-cols-[60px_1fr_36px] gap-2 items-center mb-2">
                          <span className="font-mono text-[10px] font-bold" style={{ color: row.color }}>
                            {row.label}
                          </span>
                          <div className="h-1 bg-paper-2 border border-ink relative">
                            <div
                              className="absolute top-1/2 w-2.5 h-2.5 bg-paper border-2 border-ink -translate-y-1/2 -translate-x-1/2"
                              style={{ left: row.left }}
                            />
                          </div>
                          <span className="font-mono text-[10px] font-bold text-right">{row.val}</span>
                        </div>
                      ))}
                    </>
                  )}
                </>
              )}

              {/* Audio Panel */}
              {activePanel === 'audio' && (
                <>
                  <ColorSlider label="VOL" value="80%" fillWidth="80%" fillColor="#1A6B3F" />
                  <ColorSlider label="FADE IN" value="0.5s" fillWidth="25%" fillColor="#FF3D00" />
                  <ColorSlider label="FADE OUT" value="1.0s" fillWidth="40%" fillColor="#FF3D00" />
                  <div className="font-mono text-[9px] tracking-[1.5px] font-bold mt-3.5 mb-2.5">
                    DENOISE
                  </div>
                  <div className="grid grid-cols-3 gap-0 border-1.5 border-ink">
                    {['OFF', 'LIGHT', 'HEAVY'].map((d, i) => (
                      <button
                        key={d}
                        className={`py-2 border-none font-body text-[10px] font-bold tracking-wider cursor-pointer uppercase border-r border-ink last:border-r-0 ${
                          i === 1 ? 'bg-ink text-paper' : 'bg-paper text-ink'
                        }`}
                      >
                        {d}
                      </button>
                    ))}
                  </div>
                </>
              )}

              {/* FX Panel */}
              {activePanel === 'fx' && (
                <div className="grid grid-cols-3 gap-2.5">
                  {fxItems.map((fx) => (
                    <div
                      key={fx.label}
                      className="aspect-square border-1.5 border-ink flex flex-col items-center justify-center cursor-pointer active:bg-ink active:text-paper"
                    >
                      <span className="text-[28px] mb-1.5 font-light">{fx.icon}</span>
                      <span className="font-mono text-[9px] font-bold tracking-[1.5px] uppercase">
                        {fx.label}
                      </span>
                    </div>
                  ))}
                </div>
              )}

              {/* AI Panel */}
              {activePanel === 'ai' && (
                <>
                  {aiFeatures.map((feature) => (
                    <div
                      key={feature.id}
                      className={`grid grid-cols-[44px_1fr_auto] gap-3 p-3.5 border-1.5 border-ink mb-2.5 items-center cursor-pointer active:bg-paper-2 ${
                        feature.enabled ? '' : ''
                      }`}
                    >
                      <div
                        className={`w-11 h-11 flex items-center justify-center text-xl font-light ${
                          feature.enabled ? 'bg-accent text-paper' : 'bg-ink text-paper'
                        }`}
                      >
                        {feature.icon}
                      </div>
                      <div>
                        <div className="font-fraunces text-[15px] font-bold leading-tight mb-0.5">
                          {feature.name}
                        </div>
                        <div className="font-mono text-[9px] text-ink-3 tracking-wider">
                          {feature.description}
                        </div>
                      </div>
                      <div
                        onClick={(e) => {
                          e.stopPropagation();
                          toggleAIFeature(feature.id);
                        }}
                        className={`w-10 h-[22px] border-1.5 border-ink relative cursor-pointer transition-colors ${
                          feature.enabled ? 'bg-accent border-accent' : 'bg-paper-2'
                        }`}
                      >
                        <div
                          className={`absolute top-1/2 w-3.5 h-3.5 transition-transform -translate-y-1/2 ${
                            feature.enabled
                              ? 'bg-paper translate-x-4'
                              : 'bg-ink translate-x-0.5'
                          }`}
                        />
                      </div>
                    </div>
                  ))}
                </>
              )}
            </div>
          </>
        )}
      </div>

      <BottomNav />
    </div>
  );
}
