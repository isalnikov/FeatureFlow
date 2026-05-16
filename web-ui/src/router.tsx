import { createBrowserRouter } from 'react-router-dom';
import { AppLayout } from './components/layout/AppLayout';
import { DashboardPage } from './pages/DashboardPage';
import { PortfolioPage } from './pages/PortfolioPage';
import { PlanPage } from './pages/PlanPage';
import { TeamsPage } from './pages/TeamsPage';
import { FeaturesPage } from './pages/FeaturesPage';
import { SimulationPage } from './pages/SimulationPage';
import { SettingsPage } from './pages/SettingsPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'portfolio', element: <PortfolioPage /> },
      { path: 'plan', element: <PlanPage /> },
      { path: 'teams', element: <TeamsPage /> },
      { path: 'features', element: <FeaturesPage /> },
      { path: 'simulation', element: <SimulationPage /> },
      { path: 'settings', element: <SettingsPage /> },
    ],
  },
]);
