#include <cstdint>
#include <cmath>
#include <algorithm>

#if defined(_WIN32)
    #define EPISTEME_EXPORT __declspec(dllexport)
#else
    #define EPISTEME_EXPORT __attribute__((visibility("default")))
#endif

extern "C" {

    /**
     * Performs collision detection between spheres.
     * 
     * @param positions Pointer to the [x,y,z, x,y,z, ...] array (double)
     * @param radii Pointer to the [r, r, ...] array (double)
     * @param n Number of bodies
     * @param collisions Pointer to the output [i,j, i,j, ...] array (int32_t)
     * @return Number of collisions found
     */
    EPISTEME_EXPORT int32_t detect_sphere_collisions(double* positions, double* radii, int32_t n, int32_t* collisions) {
        int32_t count = 0;
        for (int32_t i = 0; i < n; ++i) {
            double x1 = positions[i * 3];
            double y1 = positions[i * 3 + 1];
            double z1 = positions[i * 3 + 2];
            double r1 = radii[i];

            for (int32_t j = i + 1; j < n; ++j) {
                double x2 = positions[j * 3];
                double y2 = positions[j * 3 + 1];
                double z2 = positions[j * 3 + 2];
                double r2 = radii[j];

                double dx = x2 - x1;
                double dy = y2 - y1;
                double dz = z2 - z1;
                double distSq = dx * dx + dy * dy + dz * dz;
                double rSum = r1 + r2;

                if (distSq < rSum * rSum) {
                    collisions[count * 2] = i;
                    collisions[count * 2 + 1] = j;
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Resolves elastic collisions between spheres.
     */
    EPISTEME_EXPORT void resolve_sphere_collisions(double* positions, double* velocities, double* masses, int32_t n, int32_t* collisions, int32_t numCollisions) {
        for (int32_t k = 0; k < numCollisions; ++k) {
            int32_t i = collisions[k * 2];
            int32_t j = collisions[k * 2 + 1];

            double* p1 = &positions[i * 3];
            double* p2 = &positions[j * 3];
            double* v1 = &velocities[i * 3];
            double* v2 = &velocities[j * 3];
            double m1 = masses[i];
            double m2 = masses[j];

            double dx = p2[0] - p1[0];
            double dy = p2[1] - p1[1];
            double dz = p2[2] - p1[2];
            double dist = std::sqrt(dx * dx + dy * dy + dz * dz);

            if (dist == 0) continue;

            // Normal vector
            double nx = dx / dist;
            double ny = dy / dist;
            double nz = dz / dist;

            // Relative velocity
            double rvx = v2[0] - v1[0];
            double rvy = v2[1] - v1[1];
            double rvz = v2[2] - v1[2];

            // Velocity along normal
            double velAlongNormal = rvx * nx + rvy * ny + rvz * nz;

            // Do not resolve if velocities are separating
            if (velAlongNormal > 0) continue;

            // Impulse scalar
            double e = 1.0; // Restitution
            double j_imp = -(1 + e) * velAlongNormal;
            j_imp /= (1 / m1 + 1 / m2);

            // Apply impulse
            double impulseX = j_imp * nx;
            double impulseY = j_imp * ny;
            double impulseZ = j_imp * nz;

            v1[0] -= (1 / m1) * impulseX;
            v1[1] -= (1 / m1) * impulseY;
            v1[2] -= (1 / m1) * impulseZ;

            v2[0] += (1 / m2) * impulseX;
            v2[1] += (1 / m2) * impulseY;
            v2[2] += (1 / m2) * impulseZ;
        }
    }

    /**
     * Performs collision detection between spheres (float).
     */
    EPISTEME_EXPORT int32_t detect_sphere_collisions_f(float* positions, float* radii, int32_t n, int32_t* collisions) {
        int32_t count = 0;
        for (int32_t i = 0; i < n; ++i) {
            float x1 = positions[i * 3];
            float y1 = positions[i * 3 + 1];
            float z1 = positions[i * 3 + 2];
            float r1 = radii[i];

            for (int32_t j = i + 1; j < n; ++j) {
                float x2 = positions[j * 3];
                float y2 = positions[j * 3 + 1];
                float z2 = positions[j * 3 + 2];
                float r2 = radii[j];

                float dx = x2 - x1;
                float dy = y2 - y1;
                float dz = z2 - z1;
                float distSq = dx * dx + dy * dy + dz * dz;
                float rSum = r1 + r2;

                if (distSq < rSum * rSum) {
                    collisions[count * 2] = i;
                    collisions[count * 2 + 1] = j;
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Resolves elastic collisions between spheres (float).
     */
    EPISTEME_EXPORT void resolve_sphere_collisions_f(float* positions, float* velocities, float* masses, int32_t n, int32_t* collisions, int32_t numCollisions) {
        for (int32_t k = 0; k < numCollisions; ++k) {
            int32_t i = collisions[k * 2];
            int32_t j = collisions[k * 2 + 1];

            float* p1 = &positions[i * 3];
            float* p2 = &positions[j * 3];
            float* v1 = &velocities[i * 3];
            float* v2 = &velocities[j * 3];
            float m1 = masses[i];
            float m2 = masses[j];

            float dx = p2[0] - p1[0];
            float dy = p2[1] - p1[1];
            float dz = p2[2] - p1[2];
            float dist = std::sqrt(dx * dx + dy * dy + dz * dz);

            if (dist == 0) continue;

            float nx = dx / dist;
            float ny = dy / dist;
            float nz = dz / dist;

            float rvx = v2[0] - v1[0];
            float rvy = v2[1] - v1[1];
            float rvz = v2[2] - v1[2];

            float velAlongNormal = rvx * nx + rvy * ny + rvz * nz;

            if (velAlongNormal > 0) continue;

            float e = 1.0f; 
            float j_imp = -(1 + e) * velAlongNormal;
            j_imp /= (1 / m1 + 1 / m2);

            float impulseX = j_imp * nx;
            float impulseY = j_imp * ny;
            float impulseZ = j_imp * nz;

            v1[0] -= (1 / m1) * impulseX;
            v1[1] -= (1 / m1) * impulseY;
            v1[2] -= (1 / m1) * impulseZ;

            v2[0] += (1 / m2) * impulseX;
            v2[1] += (1 / m2) * impulseY;
            v2[2] += (1 / m2) * impulseZ;
        }
    }
}
