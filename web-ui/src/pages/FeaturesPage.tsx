import { useState, useEffect } from 'react';
import { useFeatures } from '../hooks/useFeatures';
import { featuresApi } from '../api/features';
import { productsApi } from '../api/products';
import { teamsApi } from '../api/teams';
import { FeatureList } from '../components/features/FeatureList';
import { FeatureDetail } from '../components/features/FeatureDetail';
import { FeatureForm } from '../components/features/FeatureForm';
import { DependencyGraph } from '../components/features/DependencyGraph';
import { Modal } from '../components/common/Modal';
import { Loading } from '../components/common/Loading';
import type { Feature, CreateFeatureRequest, Product, Team } from '../types';

export function FeaturesPage() {
  const { data, loading, error, refetch } = useFeatures({ size: 100 });
  const [selectedFeature, setSelectedFeature] = useState<Feature | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [products, setProducts] = useState<Product[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const featureList = data?.content || [];

  useEffect(() => {
    productsApi.list().then(setProducts).catch(() => setProducts([]));
    teamsApi.list().then(setTeams).catch(() => setTeams([]));
  }, []);

  const handleCreate = async (formData: CreateFeatureRequest) => {
    setIsSubmitting(true);
    setSubmitError(null);
    try {
      await featuresApi.create(formData);
      setShowCreateModal(false);
      refetch();
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : 'Failed to create feature');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleUpdate = async (formData: CreateFeatureRequest) => {
    if (!selectedFeature) return;
    setIsSubmitting(true);
    setSubmitError(null);
    try {
      await featuresApi.update(selectedFeature.id, formData);
      setShowEditModal(false);
      setSelectedFeature(null);
      refetch();
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : 'Failed to update feature');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (loading) return <Loading fullScreen label="Loading features..." />;
  if (error) return <div className="text-red-600 p-4">{error}</div>;

  return (
    <div className="flex gap-6">
      <div className="flex-1">
        <FeatureList
          features={featureList}
          onRowClick={setSelectedFeature}
          onAdd={() => setShowCreateModal(true)}
        />
      </div>

      <div className="w-96 flex-shrink-0 space-y-4">
        {selectedFeature && (
          <FeatureDetail
            feature={selectedFeature}
            onClose={() => setSelectedFeature(null)}
            onEdit={() => setShowEditModal(true)}
          />
        )}
        <DependencyGraph
          features={featureList}
          selectedFeatureId={selectedFeature?.id}
          onFeatureClick={(id) => {
            const feature = featureList.find((f) => f.id === id);
            if (feature) setSelectedFeature(feature);
          }}
        />
      </div>

      <Modal
        isOpen={showCreateModal}
        onClose={() => {
          setShowCreateModal(false);
          setSubmitError(null);
        }}
        title="Create Feature"
        size="lg"
      >
        {submitError && (
          <div className="mb-4 bg-red-50 border border-red-200 rounded-md p-3 text-sm text-red-700">
            {submitError}
          </div>
        )}
        <FeatureForm
          products={products}
          teams={teams}
          onSubmit={handleCreate}
          onCancel={() => {
            setShowCreateModal(false);
            setSubmitError(null);
          }}
          loading={isSubmitting}
        />
      </Modal>

      <Modal
        isOpen={showEditModal}
        onClose={() => {
          setShowEditModal(false);
          setSubmitError(null);
        }}
        title="Edit Feature"
        size="lg"
      >
        {submitError && (
          <div className="mb-4 bg-red-50 border border-red-200 rounded-md p-3 text-sm text-red-700">
            {submitError}
          </div>
        )}
        {selectedFeature && (
          <FeatureForm
            feature={selectedFeature}
            products={products}
            teams={teams}
            onSubmit={handleUpdate}
            onCancel={() => {
              setShowEditModal(false);
              setSubmitError(null);
            }}
            loading={isSubmitting}
          />
        )}
      </Modal>
    </div>
  );
}
