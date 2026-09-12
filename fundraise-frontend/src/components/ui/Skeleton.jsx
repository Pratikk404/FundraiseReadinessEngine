export function SkeletonLine({ className = '' }) {
  return <div className={`skeleton h-4 ${className}`} />
}

export function SkeletonCard({ className = '' }) {
  return (
    <div className={`glass-card p-6 space-y-4 ${className}`}>
      <SkeletonLine className="w-1/3 h-6" />
      <SkeletonLine className="w-full" />
      <SkeletonLine className="w-2/3" />
      <div className="flex gap-2 mt-4">
        <SkeletonLine className="w-20 h-8 rounded-full" />
        <SkeletonLine className="w-16 h-8 rounded-full" />
      </div>
    </div>
  )
}

export function SkeletonDashboard() {
  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {[1, 2, 3].map(i => (
          <SkeletonCard key={i} />
        ))}
      </div>
    </div>
  )
}
