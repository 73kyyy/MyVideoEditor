import { BrowserRouter, Routes, Route } from 'react-router-dom';
import TopNav from '@/components/TopNav';
import WorkspacePage from '@/pages/WorkspacePage';
import ClipStudioPage from '@/pages/ClipStudioPage';
import ExportHubPage from '@/pages/ExportHubPage';

export default function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-void text-paper font-body">
        <TopNav />
        <Routes>
          <Route path="/" element={<WorkspacePage />} />
          <Route path="/studio" element={<ClipStudioPage />} />
          <Route path="/export" element={<ExportHubPage />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}
