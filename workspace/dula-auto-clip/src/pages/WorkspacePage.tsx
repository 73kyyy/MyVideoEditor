import { useNavigate } from 'react-router-dom';
import { Sparkles, Zap, ArrowRight } from 'lucide-react';
import ImportZone from '@/components/ImportZone';
import AIAnalysisCard from '@/components/AIAnalysisCard';
import ProjectCard from '@/components/ProjectCard';
import GlassPanel from '@/components/GlassPanel';
import { useProjectStore } from '@/stores/projectStore';
import type { Project } from '@/types';

const quickActions = [
  { label: 'TikTok Reel', icon: '📱', desc: '9:16 vertical' },
  { label: 'YouTube Short', icon: '▶️', desc: '60s max' },
  { label: 'Instagram Reel', icon: '📸', desc: '9:16 vertical' },
  { label: 'Batch Export', icon: '📦', desc: 'All platforms' },
];

export default function WorkspacePage() {
  const navigate = useNavigate();
  const { projects } = useProjectStore();

  const handleOpenProject = (project: Project) => {
    navigate('/studio');
  };

  return (
    <div className="min-h-screen bg-void">
      {/* Hero Section */}
      <div className="px-6 pt-8 pb-6 border-b border-dim/20">
        <div className="flex items-center gap-2 mb-3">
          <Sparkles className="w-5 h-5 text-neon" />
          <span className="font-mono text-[10px] text-neon tracking-[3px] uppercase">AI-Powered</span>
        </div>
        <h1 className="font-grotesk text-4xl font-bold tracking-tight text-paper mb-2">
          Auto <span className="text-neon">Clip</span>.
        </h1>
        <p className="font-body text-sm text-muted max-w-lg">
          Drop your footage. AI finds the best moments. You ship the clips.
        </p>
      </div>

      <div className="px-6 py-6 grid grid-cols-12 gap-6">
        {/* Left Column */}
        <div className="col-span-8 space-y-6">
          {/* Import Zone */}
          <ImportZone />

          {/* Project Grid */}
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-grotesk font-semibold text-lg text-paper">Recent Projects</h2>
              <span className="font-mono text-[10px] text-muted tracking-wider">{projects.length} TOTAL</span>
            </div>
            <div className="grid grid-cols-3 gap-4">
              {projects.map((project) => (
                <ProjectCard key={project.id} project={project} onClick={handleOpenProject} />
              ))}
            </div>
          </div>
        </div>

        {/* Right Column */}
        <div className="col-span-4 space-y-5">
          {/* AI Analysis */}
          <AIAnalysisCard />

          {/* Quick Actions */}
          <GlassPanel className="p-5">
            <div className="flex items-center gap-2 mb-4">
              <Zap className="w-4 h-4 text-heat" />
              <span className="font-grotesk font-semibold text-sm text-paper">Quick Actions</span>
            </div>
            <div className="grid grid-cols-2 gap-2">
              {quickActions.map((action) => (
                <button
                  key={action.label}
                  className="p-3 rounded-lg bg-surface/60 border border-dim/20 text-left hover:border-neon/30 hover:bg-neon/5 transition-all duration-200 group"
                >
                  <span className="text-lg mb-1 block">{action.icon}</span>
                  <span className="font-grotesk text-xs font-medium text-paper group-hover:text-neon transition-colors block">
                    {action.label}
                  </span>
                  <span className="font-mono text-[9px] text-muted">{action.desc}</span>
                </button>
              ))}
            </div>
          </GlassPanel>

          {/* Stats */}
          <GlassPanel className="p-5">
            <div className="grid grid-cols-3 gap-3 text-center">
              <div>
                <div className="font-grotesk font-bold text-xl text-paper">47</div>
                <div className="font-mono text-[9px] text-muted tracking-wider uppercase">Clips</div>
              </div>
              <div>
                <div className="font-grotesk font-bold text-xl text-neon">12</div>
                <div className="font-mono text-[9px] text-muted tracking-wider uppercase">Exported</div>
              </div>
              <div>
                <div className="font-grotesk font-bold text-xl text-violet">89</div>
                <div className="font-mono text-[9px] text-muted tracking-wider uppercase">Avg Score</div>
              </div>
            </div>
          </GlassPanel>

          {/* CTA */}
          <button
            onClick={() => navigate('/studio')}
            className="w-full py-3.5 rounded-xl bg-gradient-to-r from-neon/20 to-violet/20 border border-neon/30 font-grotesk font-semibold text-sm text-neon hover:from-neon/30 hover:to-violet/30 transition-all duration-300 flex items-center justify-center gap-2"
          >
            Open Clip Studio
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
}
