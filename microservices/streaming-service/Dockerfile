FROM node:16.15.0-alpine AS development

WORKDIR application

# Copy application dependency manifests to the container image.
# A wildcard is used to ensure copying both package.json AND package-lock.json (when available).
# Copying this first prevents re-running npm install on every code change.
COPY --chown=node:node package*.json ./

RUN npm ci

# Bundle app source
COPY --chown=node:node . .

# Use the node user from the image (instead of the root user)
USER node

FROM node:16.15.0-alpine AS build

WORKDIR application

COPY --chown=node:node package*.json ./

# In order to run `npm run build` we need access to the Nest CLI which is a dev dependency.
# In the previous development stage we ran `npm ci` which installed all dependencies,
# so we can copy over the node_modules directory from the development image
COPY --chown=node:node --from=development application/node_modules ./node_modules

COPY --chown=node:node . .

RUN npm run build

ENV NODE_ENV production

# Running `npm ci` removes the existing node_modules directory and
# passing in --only=production ensures that only the production dependencies are installed.
# This ensures that the node_modules directory is as optimized as possible
RUN npm ci --only=production && npm cache clean --force

USER node

FROM node:16.15.0-alpine AS production

# Copy the bundled code from the build stage to the production image
COPY --chown=node:node --from=build application/node_modules ./node_modules
COPY --chown=node:node --from=build application/dist ./dist

EXPOSE 3000

CMD [ "node", "dist/src/main.js" ]
