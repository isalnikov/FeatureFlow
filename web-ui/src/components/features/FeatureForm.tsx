import { useState, FormEvent } from 'react';
import type { Feature, CreateFeatureRequest, ClassOfService, Product, Team } from '../../types';
import { Button } from '../common/Button';

interface FeatureFormProps {
  feature?: Feature | null;
  products: Product[];
  teams: Team[];
  onSubmit: (data: CreateFeatureRequest) => void;
  onCancel: () => void;
  loading?: boolean;
}

export function FeatureForm({ feature, products, teams, onSubmit, onCancel, loading }: FeatureFormProps) {
  const [title, setTitle] = useState(feature?.title || '');
  const [description, setDescription] = useState(feature?.description || '');
  const [businessValue, setBusinessValue] = useState(feature?.businessValue || 50);
  const [classOfService, setClassOfService] = useState<ClassOfService>(feature?.classOfService || 'STANDARD');
  const [deadline, setDeadline] = useState(feature?.deadline || '');
  const [productIds, setProductIds] = useState<string[]>(feature?.productIds || []);
  const [backendHours, setBackendHours] = useState(feature?.effortEstimate.backendHours || 0);
  const [frontendHours, setFrontendHours] = useState(feature?.effortEstimate.frontendHours || 0);
  const [qaHours, setQaHours] = useState(feature?.effortEstimate.qaHours || 0);
  const [devopsHours, setDevopsHours] = useState(feature?.effortEstimate.devopsHours || 0);
  const [requiredExpertise, setRequiredExpertise] = useState<string[]>(feature?.requiredExpertise || []);
  const [canSplit, setCanSplit] = useState(feature?.canSplit || false);
  const [expertiseInput, setExpertiseInput] = useState('');

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();

    const data: CreateFeatureRequest = {
      title,
      description,
      businessValue,
      classOfService,
      productIds,
      effortEstimate: {
        backendHours,
        frontendHours,
        qaHours,
        devopsHours,
      },
      stochasticEstimate: null,
      dependencies: feature?.dependencies || [],
      requiredExpertise,
      canSplit,
    };

    onSubmit(data);
  };

  const toggleProduct = (productId: string) => {
    setProductIds((prev) =>
      prev.includes(productId) ? prev.filter((id) => id !== productId) : [...prev, productId],
    );
  };

  const addExpertise = () => {
    if (expertiseInput.trim() && !requiredExpertise.includes(expertiseInput.trim())) {
      setRequiredExpertise((prev) => [...prev, expertiseInput.trim()]);
      setExpertiseInput('');
    }
  };

  const removeExpertise = (tag: string) => {
    setRequiredExpertise((prev) => prev.filter((t) => t !== tag));
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Title</label>
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
          className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          placeholder="Feature title"
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
        <textarea
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          rows={3}
          className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          placeholder="Feature description"
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Business Value</label>
          <input
            type="number"
            value={businessValue}
            onChange={(e) => setBusinessValue(Number(e.target.value))}
            min={0}
            max={100}
            className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Class of Service</label>
          <select
            value={classOfService}
            onChange={(e) => setClassOfService(e.target.value as ClassOfService)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="EXPEDITE">Expedite</option>
            <option value="FIXED_DATE">Fixed Date</option>
            <option value="STANDARD">Standard</option>
            <option value="FILLER">Filler</option>
          </select>
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Deadline (optional)</label>
        <input
          type="date"
          value={deadline}
          onChange={(e) => setDeadline(e.target.value)}
          className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Products</label>
        <div className="flex flex-wrap gap-2">
          {products.map((product) => (
            <button
              key={product.id}
              type="button"
              onClick={() => toggleProduct(product.id)}
              className={`px-3 py-1.5 text-sm rounded-md border transition-colors ${
                productIds.includes(product.id)
                  ? 'bg-brand-100 border-brand-300 text-brand-700'
                  : 'bg-white border-gray-300 text-gray-700 hover:bg-gray-50'
              }`}
            >
              {product.name}
            </button>
          ))}
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Effort Estimates (hours)</label>
        <div className="grid grid-cols-4 gap-3">
          <div>
            <label className="block text-xs text-gray-500 mb-1">Backend</label>
            <input
              type="number"
              value={backendHours}
              onChange={(e) => setBackendHours(Number(e.target.value))}
              min={0}
              className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>
          <div>
            <label className="block text-xs text-gray-500 mb-1">Frontend</label>
            <input
              type="number"
              value={frontendHours}
              onChange={(e) => setFrontendHours(Number(e.target.value))}
              min={0}
              className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>
          <div>
            <label className="block text-xs text-gray-500 mb-1">QA</label>
            <input
              type="number"
              value={qaHours}
              onChange={(e) => setQaHours(Number(e.target.value))}
              min={0}
              className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>
          <div>
            <label className="block text-xs text-gray-500 mb-1">DevOps</label>
            <input
              type="number"
              value={devopsHours}
              onChange={(e) => setDevopsHours(Number(e.target.value))}
              min={0}
              className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Required Expertise</label>
        <div className="flex gap-2 mb-2">
          <input
            type="text"
            value={expertiseInput}
            onChange={(e) => setExpertiseInput(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), addExpertise())}
            className="flex-1 px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            placeholder="Add expertise tag"
          />
          <Button type="button" variant="secondary" size="sm" onClick={addExpertise}>
            Add
          </Button>
        </div>
        <div className="flex flex-wrap gap-2">
          {requiredExpertise.map((tag) => (
            <span key={tag} className="inline-flex items-center gap-1 px-2 py-1 bg-gray-100 rounded-md text-sm">
              {tag}
              <button type="button" onClick={() => removeExpertise(tag)} className="text-gray-400 hover:text-gray-600">
                ×
              </button>
            </span>
          ))}
        </div>
      </div>

      <div className="flex items-center gap-2">
        <input
          type="checkbox"
          id="canSplit"
          checked={canSplit}
          onChange={(e) => setCanSplit(e.target.checked)}
          className="rounded border-gray-300"
        />
        <label htmlFor="canSplit" className="text-sm text-gray-700">
          Can be split into parallel epics
        </label>
      </div>

      <div className="flex justify-end gap-3 pt-4 border-t border-gray-200">
        <Button type="button" variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" loading={loading}>
          {feature ? 'Update Feature' : 'Create Feature'}
        </Button>
      </div>
    </form>
  );
}
