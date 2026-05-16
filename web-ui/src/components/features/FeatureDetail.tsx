import type { Feature, ClassOfService } from '../../types';
import { Badge } from '../common/Badge';
import { Button } from '../common/Button';

interface FeatureDetailProps {
  feature: Feature;
  onClose: () => void;
  onEdit?: () => void;
}

const classOfServiceLabels: Record<ClassOfService, string> = {
  EXPEDITE: 'Expedite',
  FIXED_DATE: 'Fixed Date',
  STANDARD: 'Standard',
  FILLER: 'Filler',
};

const classOfServiceVariants: Record<ClassOfService, 'error' | 'warning' | 'default' | 'info'> = {
  EXPEDITE: 'error',
  FIXED_DATE: 'warning',
  STANDARD: 'default',
  FILLER: 'info',
};

export function FeatureDetail({ feature, onClose, onEdit }: FeatureDetailProps) {
  const totalEffort =
    feature.effortEstimate.backendHours +
    feature.effortEstimate.frontendHours +
    feature.effortEstimate.qaHours +
    feature.effortEstimate.devopsHours;

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-4">
      <div className="flex items-start justify-between mb-4">
        <div>
          <h3 className="text-lg font-semibold text-gray-900">{feature.title}</h3>
          <Badge variant={classOfServiceVariants[feature.classOfService]} className="mt-1">
            {classOfServiceLabels[feature.classOfService]}
          </Badge>
        </div>
        <div className="flex items-center gap-2">
          {onEdit && (
            <Button variant="ghost" size="sm" onClick={onEdit}>
              Edit
            </Button>
          )}
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      <p className="text-sm text-gray-600 mb-4">{feature.description}</p>

      <div className="space-y-4">
        <div>
          <h4 className="text-xs font-medium text-gray-500 uppercase mb-2">Business Value</h4>
          <div className="text-2xl font-bold text-gray-900">{feature.businessValue}</div>
        </div>

        <div>
          <h4 className="text-xs font-medium text-gray-500 uppercase mb-2">Effort Estimates</h4>
          <div className="grid grid-cols-4 gap-3">
            <div className="bg-blue-50 rounded-md p-2 text-center">
              <div className="text-lg font-bold text-blue-700">{feature.effortEstimate.backendHours}</div>
              <div className="text-xs text-blue-600">Backend</div>
            </div>
            <div className="bg-green-50 rounded-md p-2 text-center">
              <div className="text-lg font-bold text-green-700">{feature.effortEstimate.frontendHours}</div>
              <div className="text-xs text-green-600">Frontend</div>
            </div>
            <div className="bg-amber-50 rounded-md p-2 text-center">
              <div className="text-lg font-bold text-amber-700">{feature.effortEstimate.qaHours}</div>
              <div className="text-xs text-amber-600">QA</div>
            </div>
            <div className="bg-purple-50 rounded-md p-2 text-center">
              <div className="text-lg font-bold text-purple-700">{feature.effortEstimate.devopsHours}</div>
              <div className="text-xs text-purple-600">DevOps</div>
            </div>
          </div>
          <div className="mt-2 text-sm text-gray-600">Total: {totalEffort} hours</div>
        </div>

        {feature.deadline && (
          <div>
            <h4 className="text-xs font-medium text-gray-500 uppercase mb-1">Deadline</h4>
            <div className="text-sm text-gray-900">{feature.deadline}</div>
          </div>
        )}

        {feature.dependencies.length > 0 && (
          <div>
            <h4 className="text-xs font-medium text-gray-500 uppercase mb-2">Dependencies</h4>
            <div className="space-y-1">
              {feature.dependencies.map((depId) => (
                <div key={depId} className="text-sm text-gray-700 bg-gray-50 px-2 py-1 rounded">
                  {depId}
                </div>
              ))}
            </div>
          </div>
        )}

        {feature.requiredExpertise.length > 0 && (
          <div>
            <h4 className="text-xs font-medium text-gray-500 uppercase mb-2">Required Expertise</h4>
            <div className="flex flex-wrap gap-1">
              {feature.requiredExpertise.map((tag) => (
                <span key={tag} className="px-2 py-1 bg-gray-100 rounded text-xs">
                  {tag}
                </span>
              ))}
            </div>
          </div>
        )}

        {feature.canSplit && (
          <div>
            <Badge variant="info">Can be split into parallel epics</Badge>
          </div>
        )}
      </div>
    </div>
  );
}
