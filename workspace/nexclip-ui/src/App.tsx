import { BrowserRouter, Routes, Route } from 'react-router-dom';
import PhoneFrame from '@/components/PhoneFrame';
import StudioPage from '@/pages/StudioPage';
import EditorPage from '@/pages/EditorPage';
import SettingsPage from '@/pages/SettingsPage';

export default function App() {
  return (
    <BrowserRouter>
      <PhoneFrame>
        <Routes>
          <Route path="/" element={<StudioPage />} />
          <Route path="/editor" element={<EditorPage />} />
          <Route path="/settings" element={<SettingsPage />} />
        </Routes>
      </PhoneFrame>
    </BrowserRouter>
  );
}
