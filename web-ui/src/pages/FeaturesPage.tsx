import { useState } from 'react';
import { useFeatures } from '../hooks/useFeatures';
import { FeatureList } from '../components/features/FeatureList';
import { FeatureDetail } from '../components/features/FeatureDetail';
import { FeatureForm } from '../components/features/FeatureForm';
import { DependencyGraph } from '../components/features/DependencyGraph';
import { Modal } from '../components/common/Modal';
import { Loading } from '../components/common/Loading';
import type { Feature, CreateFeatureRequest } from '../types';

export function FeaturesPage() {
  const { data, loading, error, refetch } = useFeatures({ size: 100 });
  const [selectedFeature, setSelectedFeature] = useState<Feature | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);

  const featureList = data?.content || [];

  const handleCreate = async (formData: CreateFeatureRequest) => {
    console.log('Creating feature:', formData);
    setShowCreateModal(false);
    refetch();
  };

  const handleUpdate = async (formData: CreateFeatureRequest) => {
    console.log('Updating feature:', selectedFeature?.id, formData);
    setShowEditModal(false);
    setSelectedFeature(null);
    refetch();
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
        onClose={() => setShowCreateModal(false)}
        title="Create Feature"
        size="lg"
      >
        <FeatureForm
          products={[]}
          teams={[]}
          onSubmit={handleCreate}
          onCancel={() => setShowCreateModal(false)}
        />
      </Modal>

      <Modal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
        title="Edit Feature"
        size="lg"
      >
        {selectedFeature && (
          <FeatureForm
            feature={selectedFeature}
            products={[]}
            teams={[]}
            onSubmit={handleUpdate}
            onCancel={() => setShowEditModal(false)}
          />
        )}
      </Modal>
    </div>
  );
}
