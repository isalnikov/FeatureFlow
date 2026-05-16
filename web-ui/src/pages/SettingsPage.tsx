import { useState } from 'react';
import { integrationsApi } from '../api/integrations';
import { Button } from '../components/common/Button';

export function SettingsPage() {
  const [planningParams, setPlanningParams] = useState({
    w1Ttm: 1.0,
    w2Underutilization: 0.5,
    w3DeadlinePenalty: 2.0,
    maxParallelFeatures: 3,
    initialTemperature: 1000.0,
    coolingRate: 0.95,
    minTemperature: 0.1,
    maxIterations: 10000,
    monteCarloIterations: 1000,
    confidenceLevel: 0.95,
    defaultFocusFactor: 0.7,
    defaultBugReserve: 0.2,
    defaultTechDebtReserve: 0.1,
  });

  const [integrationConfig, setIntegrationConfig] = useState({
    jiraUrl: '',
    jiraUsername: '',
    jiraToken: '',
    adoUrl: '',
    adoToken: '',
  });

  const [saveStatus, setSaveStatus] = useState<string | null>(null);
  const [testStatus, setTestStatus] = useState<Record<string, string>>({});
  const [testing, setTesting] = useState<Record<string, boolean>>({});

  const handleSavePlanning = () => {
    localStorage.setItem('featureflow_planning_params', JSON.stringify(planningParams));
    setSaveStatus('Planning parameters saved successfully');
    setTimeout(() => setSaveStatus(null), 3000);
  };

  const handleTestConnection = async (type: 'jira' | 'ado') => {
    setTesting((prev) => ({ ...prev, [type]: true }));
    setTestStatus((prev) => ({ ...prev, [type]: '' }));

    try {
      if (type === 'jira') {
        await integrationsApi.testConnection('jira', {
          baseUrl: integrationConfig.jiraUrl,
          authType: 'basic',
          username: integrationConfig.jiraUsername,
          apiToken: integrationConfig.jiraToken,
        });
        setTestStatus((prev) => ({ ...prev, [type]: 'Connection successful' }));
      } else {
        await integrationsApi.testConnection('ado', {
          baseUrl: integrationConfig.adoUrl,
          authType: 'token',
          apiToken: integrationConfig.adoToken,
        });
        setTestStatus((prev) => ({ ...prev, [type]: 'Connection successful' }));
      }
    } catch (err) {
      setTestStatus((prev) => ({
        ...prev,
        [type]: err instanceof Error ? err.message : 'Connection failed',
      }));
    } finally {
      setTesting((prev) => ({ ...prev, [type]: false }));
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Settings</h1>

      <div className="space-y-6">
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Planning Algorithm Parameters</h2>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">TTM Weight (w1)</label>
              <input
                type="number"
                step="0.1"
                value={planningParams.w1Ttm}
                onChange={(e) => setPlanningParams((p) => ({ ...p, w1Ttm: Number(e.target.value) }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Underutilization Weight (w2)</label>
              <input
                type="number"
                step="0.1"
                value={planningParams.w2Underutilization}
                onChange={(e) => setPlanningParams((p) => ({ ...p, w2Underutilization: Number(e.target.value) }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Deadline Penalty (w3)</label>
              <input
                type="number"
                step="0.1"
                value={planningParams.w3DeadlinePenalty}
                onChange={(e) => setPlanningParams((p) => ({ ...p, w3DeadlinePenalty: Number(e.target.value) }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Max Parallel Features</label>
              <input
                type="number"
                value={planningParams.maxParallelFeatures}
                onChange={(e) => setPlanningParams((p) => ({ ...p, maxParallelFeatures: Number(e.target.value) }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Initial Temperature</label>
              <input
                type="number"
                value={planningParams.initialTemperature}
                onChange={(e) => setPlanningParams((p) => ({ ...p, initialTemperature: Number(e.target.value) }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Cooling Rate</label>
              <input
                type="number"
                step="0.01"
                value={planningParams.coolingRate}
                onChange={(e) => setPlanningParams((p) => ({ ...p, coolingRate: Number(e.target.value) }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Max Iterations</label>
              <input
                type="number"
                value={planningParams.maxIterations}
                onChange={(e) => setPlanningParams((p) => ({ ...p, maxIterations: Number(e.target.value) }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Monte Carlo Iterations</label>
              <input
                type="number"
                value={planningParams.monteCarloIterations}
                onChange={(e) => setPlanningParams((p) => ({ ...p, monteCarloIterations: Number(e.target.value) }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
              />
            </div>
          </div>
          <div className="mt-4 flex items-center gap-3">
            <Button onClick={handleSavePlanning}>Save Planning Parameters</Button>
            {saveStatus && <span className="text-sm text-green-600">{saveStatus}</span>}
          </div>
        </div>

        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Default Team Settings</h2>
          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Focus Factor</label>
              <input
                type="number"
                step="0.05"
                value={planningParams.defaultFocusFactor}
                onChange={(e) => setPlanningParams((p) => ({ ...p, defaultFocusFactor: Number(e.target.value) }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Bug Reserve</label>
              <input
                type="number"
                step="0.05"
                value={planningParams.defaultBugReserve}
                onChange={(e) => setPlanningParams((p) => ({ ...p, defaultBugReserve: Number(e.target.value) }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Tech Debt Reserve</label>
              <input
                type="number"
                step="0.05"
                value={planningParams.defaultTechDebtReserve}
                onChange={(e) => setPlanningParams((p) => ({ ...p, defaultTechDebtReserve: Number(e.target.value) }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
              />
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Integrations</h2>

          <div className="space-y-4">
            <div className="border border-gray-200 rounded-md p-4">
              <h3 className="text-sm font-medium text-gray-900 mb-3">Jira</h3>
              <div className="grid grid-cols-2 gap-3">
                <input
                  type="text"
                  placeholder="https://your-domain.atlassian.net"
                  value={integrationConfig.jiraUrl}
                  onChange={(e) => setIntegrationConfig((c) => ({ ...c, jiraUrl: e.target.value }))}
                  className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                />
                <input
                  type="text"
                  placeholder="Email"
                  value={integrationConfig.jiraUsername}
                  onChange={(e) => setIntegrationConfig((c) => ({ ...c, jiraUsername: e.target.value }))}
                  className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                />
                <input
                  type="password"
                  placeholder="API Token"
                  value={integrationConfig.jiraToken}
                  onChange={(e) => setIntegrationConfig((c) => ({ ...c, jiraToken: e.target.value }))}
                  className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                />
                <div className="flex items-center gap-2">
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={() => handleTestConnection('jira')}
                    loading={testing.jira}
                  >
                    Test Connection
                  </Button>
                  {testStatus.jira && (
                    <span className={`text-xs ${testStatus.jira.includes('successful') ? 'text-green-600' : 'text-red-600'}`}>
                      {testStatus.jira}
                    </span>
                  )}
                </div>
              </div>
            </div>

            <div className="border border-gray-200 rounded-md p-4">
              <h3 className="text-sm font-medium text-gray-900 mb-3">Azure DevOps</h3>
              <div className="grid grid-cols-2 gap-3">
                <input
                  type="text"
                  placeholder="Organization URL"
                  value={integrationConfig.adoUrl}
                  onChange={(e) => setIntegrationConfig((c) => ({ ...c, adoUrl: e.target.value }))}
                  className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                />
                <input
                  type="password"
                  placeholder="Personal Access Token"
                  value={integrationConfig.adoToken}
                  onChange={(e) => setIntegrationConfig((c) => ({ ...c, adoToken: e.target.value }))}
                  className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                />
                <div className="flex items-center gap-2 col-span-2">
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={() => handleTestConnection('ado')}
                    loading={testing.ado}
                  >
                    Test Connection
                  </Button>
                  {testStatus.ado && (
                    <span className={`text-xs ${testStatus.ado.includes('successful') ? 'text-green-600' : 'text-red-600'}`}>
                      {testStatus.ado}
                    </span>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
