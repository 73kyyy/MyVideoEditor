import { useNavigate } from 'react-router-dom';
import StatusBar from '@/components/StatusBar';
import BottomNav from '@/components/BottomNav';
import { useProjectStore } from '@/stores/projectStore';
import { useEditorStore } from '@/stores/editorStore';

const tabs = [
  { key: 'all' as const, label: 'All', count: '12' },
  { key: 'draft' as const, label: 'Draft', count: '05' },
  { key: 'done' as const, label: 'Done', count: '07' },
];

function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

export default function StudioPage() {
  const navigate = useNavigate();
  const { projects, activeTab, setActiveTab } = useProjectStore();
  const { setCurrentProject } = useEditorStore();

  const filtered = projects.filter((p) => {
    if (activeTab === 'all') return true;
    if (activeTab === 'ai') return p.aiEnhanced;
    return p.status === activeTab;
  });

  const handleOpenProject = (project: typeof projects[0]) => {
    setCurrentProject(project);
    navigate('/editor');
  };

  return (
    <div className="h-full flex flex-col">
      <StatusBar />

      {/* Hero */}
      <div className="px-6 pt-5 pb-4 border-b-1.5 border-ink flex-shrink-0">
        <div className="font-mono text-[10px] tracking-[2px] uppercase text-ink-3 mb-2">
          Vol. 001 / NEXCLIP
        </div>
        <div className="font-fraunces text-[54px] leading-[0.92] font-black tracking-[-2px] mb-1.5">
          MAKE<br />VIDEO <em className="font-light text-accent">BOLD</em>.
        </div>
        <div className="text-[13px] text-ink-2 leading-snug max-w-[280px]">
          A professional editing suite in your pocket. AI-powered, frame-perfect, and yours.
        </div>
      </div>

      {/* Meta */}
      <div className="flex justify-between items-center px-6 py-3 border-b-1.5 border-ink font-mono text-[10px] tracking-wider flex-shrink-0">
        <div>
          <div className="text-ink-3 uppercase">Projects</div>
          <div className="text-sm font-bold text-ink">{projects.length}</div>
        </div>
        <div>
          <div className="text-ink-3 uppercase">Storage</div>
          <div className="text-sm font-bold text-ink">4.2 GB</div>
        </div>
        <div>
          <div className="text-ink-3 uppercase">AI Credits</div>
          <div className="text-sm font-bold text-ink">847</div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b-1.5 border-ink flex-shrink-0">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`flex-1 py-3.5 px-2 text-[11px] font-semibold text-center uppercase tracking-[1.5px] cursor-pointer border-none bg-transparent font-body relative transition-colors ${
              activeTab === tab.key ? 'text-ink' : 'text-ink-2'
            }`}
          >
            {tab.label}
            <span className="block font-mono text-[9px] text-ink-3 mt-0.5 font-normal tracking-normal">
              {tab.count}
            </span>
            {activeTab === tab.key && (
              <span className="absolute left-0 right-0 bottom-[-1.5px] h-[3px] bg-accent" />
            )}
          </button>
        ))}
      </div>

      {/* Project List */}
      <div className="flex-1 overflow-y-auto scrollbar-none pb-20">
        {filtered.map((project) => (
          <div
            key={project.id}
            onClick={() => handleOpenProject(project)}
            className="grid grid-cols-[64px_1fr_auto] gap-3.5 px-6 py-4 items-center border-b border-ink cursor-pointer relative transition-colors active:bg-paper-2 hover:before:bg-accent"
            style={{ gridTemplateColumns: '64px 1fr auto' }}
          >
            <div
              className="before:content-[''] before:absolute before:left-0 before:top-0 before:bottom-0 before:w-[3px] before:bg-transparent before:transition-colors"
            >
              <div className="w-16 h-20 bg-ink relative overflow-hidden">
                <div
                  className="absolute inset-0"
                  style={{ background: project.thumbnail }}
                />
                <span className="absolute bottom-[3px] left-[3px] font-mono text-[8px] text-accent-2 font-bold">
                  R{project.id}
                </span>
              </div>
            </div>
            <div>
              <div className="font-fraunces text-lg font-bold leading-tight mb-1">
                {project.name}
              </div>
              <div className="font-mono text-[10px] text-ink-3 tracking-wide">
                {formatDuration(project.duration)} · {project.resolution} · {project.fps}FPS
              </div>
              <div className="flex gap-1 mt-1.5 flex-wrap">
                {project.aiEnhanced && (
                  <span className="font-mono text-[9px] px-1.5 py-0.5 bg-accent text-paper font-semibold tracking-wide">
                    AI
                  </span>
                )}
                {project.aiFeatures.includes('interpolation') && (
                  <span className="font-mono text-[9px] px-1.5 py-0.5 border border-ink text-ink font-semibold tracking-wide">
                    60FPS
                  </span>
                )}
                {project.aiFeatures.includes('caption') && (
                  <span className="font-mono text-[9px] px-1.5 py-0.5 border border-ink text-ink font-semibold tracking-wide">
                    CAPTION
                  </span>
                )}
              </div>
            </div>
            <span className="font-mono text-lg text-ink font-bold">→</span>
          </div>
        ))}

        {/* New Project */}
        <div
          onClick={() => {
            const newProject = {
              id: String(projects.length + 1),
              name: 'New Project',
              thumbnail: 'linear-gradient(135deg,#0A0A0A,#3A3A38)',
              duration: 0,
              resolution: '1080p',
              fps: 30,
              aiEnhanced: false,
              aiFeatures: [],
              status: 'draft' as const,
              createdAt: new Date(),
              updatedAt: new Date(),
            };
            handleOpenProject(newProject);
          }}
          className="mx-6 my-4 px-6 py-4 bg-ink text-paper flex justify-between items-center cursor-pointer active:bg-accent"
        >
          <span className="font-fraunces text-[22px] font-bold italic">
            Start a new project →
          </span>
          <span className="w-10 h-10 bg-paper text-ink flex items-center justify-center text-2xl font-light">
            +
          </span>
        </div>
      </div>

      <BottomNav />
    </div>
  );
}
