"use strict";

const state = {
    token: null,
    role: null,
    categories: [],
    entities: [],
    editingId: null,
    geometry: null,
    drawVertices: [],
    drawMarker: null,
    drawPreview: null,
    drawingLocked: false,
    prevGeometryType: "Point",
    map: null,
    entityLayer: null,
    osmLayer: null,
    localLayer: null,
    baseControl: null,
    activeBasemap: "osm",
    osmConsecutiveErrors: 0,
    fallbackActive: false,
    coordinatesControl: null,
    basemapNoticeTimer: null,
    selectedId: null,
    layersById: {},
};

const HIGHLIGHT_COLOR = "#ff9800";

const highlightMarkerStyle = {
    radius: 11,
    color: HIGHLIGHT_COLOR,
    weight: 3,
    fillColor: HIGHLIGHT_COLOR,
    fillOpacity: 0.3,
};

const NATURE_LABELS = {
    POINT_OF_INTEREST: "Punto de interés",
    ROAD: "Vía",
    NEIGHBORHOOD: "Barrio",
    INSTITUTION: "Institución",
    COMMERCIAL_ESTABLISHMENT: "Establecimiento comercial",
    ZONE_OF_INTEREST: "Zona de interés",
};

const CATEGORY_LABELS = {
    TURISMO: "Turismo",
    VIA: "Vía",
    BARRIO: "Barrio",
    INSTITUCION: "Institución",
    COMERCIO: "Comercio",
    ZONA: "Zona de interés",
};

const GEOMETRY_LABELS = {
    Point: "Punto",
    LineString: "Línea",
    Polygon: "Polígono",
};

const NATURE_ICON_PATHS = {
    POINT_OF_INTEREST: '<path d="M12 21s-7-5.3-7-11a7 7 0 1 1 14 0c0 5.7-7 11-7 11z"/><circle cx="12" cy="10" r="2.5"/>',
    ROAD: '<circle cx="12" cy="5" r="1.5"/><circle cx="12" cy="12" r="1.5"/><circle cx="12" cy="19" r="1.5"/><path d="M12 6.5v4M12 13.5v4"/>',
    NEIGHBORHOOD: '<path d="M3 11l9-7 9 7"/><path d="M5 9.5V20h14V9.5"/><path d="M9 20v-6h6v6"/>',
    INSTITUTION: '<path d="M3 21h18"/><path d="M4 21V10h16v11"/><path d="m12 3-8 5h16z"/><path d="M12 9v4"/>',
    COMMERCIAL_ESTABLISHMENT: '<path d="M6 7h12l1.5 13h-15z"/><path d="M9 10V6a3 3 0 0 1 6 0v4"/>',
    ZONE_OF_INTEREST: '<path d="m12 3 9 4.5-9 4.5L3 7.5z"/><path d="m3 12.5 9 4.5 9-4.5"/>',
};

function natureIcon(nature) {
    const paths = NATURE_ICON_PATHS[nature] || NATURE_ICON_PATHS.POINT_OF_INTEREST;
    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">' + paths + "</svg>";
}

function natureLabel(nature) {
    return NATURE_LABELS[nature] || nature;
}

function categoryLabel(category) {
    return CATEGORY_LABELS[category] || category;
}

function entityMarkerIcon() {
    return L.divIcon({
        className: "entity-marker",
        html: '<span class="entity-marker-pin"></span>',
        iconSize: [20, 24],
        iconAnchor: [10, 24],
        popupAnchor: [0, -24],
        tooltipAnchor: [0, -18],
    });
}

const els = {
    loginView: document.getElementById("login-view"),
    appView: document.getElementById("app-view"),
    loginForm: document.getElementById("login-form"),
    loginError: document.getElementById("login-error"),
    username: document.getElementById("username"),
    password: document.getElementById("password"),
    togglePassword: document.getElementById("toggle-password"),
    loginSubmit: document.getElementById("login-submit"),
    userAvatar: document.getElementById("user-avatar"),
    currentUser: document.getElementById("current-user"),
    currentRole: document.getElementById("current-role"),
    logoutBtn: document.getElementById("logout-btn"),
    queryForm: document.getElementById("query-form"),
    qCategory: document.getElementById("q-category"),
    qText: document.getElementById("q-text"),
    qAttribute: document.getElementById("q-attribute"),
    qLat: document.getElementById("q-lat"),
    qLon: document.getElementById("q-lon"),
    qRadius: document.getElementById("q-radius"),
    queryClear: document.getElementById("query-clear"),
    entityList: document.getElementById("entity-list"),
    listStatus: document.getElementById("list-status"),
    resultsCount: document.getElementById("results-count"),
    emptyState: document.getElementById("empty-state"),
    tabSearch: document.getElementById("tab-search"),
    tabRegister: document.getElementById("tab-register"),
    searchTab: document.getElementById("search-tab"),
    registerTab: document.getElementById("register-tab"),
    formTitle: document.getElementById("form-title"),
    entityForm: document.getElementById("entity-form"),
    entityId: document.getElementById("entity-id"),
    eName: document.getElementById("e-name"),
    eDescription: document.getElementById("e-description"),
    eNature: document.getElementById("e-nature"),
    eCategory: document.getElementById("e-category"),
    eAttributes: document.getElementById("e-attributes"),
    eType: document.getElementById("e-type"),
    geometryStatus: document.getElementById("geometry-status"),
    geometryClear: document.getElementById("geometry-clear"),
    geometryFinish: document.getElementById("geometry-finish"),
    formCancel: document.getElementById("form-cancel"),
    formStatus: document.getElementById("form-status"),
    mapInfo: document.getElementById("map-info"),
    basemapNotice: document.getElementById("basemap-notice"),
};

function show(view) {
    els.loginView.classList.toggle("hidden", view !== "login");
    els.appView.classList.toggle("hidden", view !== "app");
}

function showMessage(el, text, kind) {
    el.textContent = text;
    el.classList.remove("info", "error");
    el.classList.add(kind || "info");
    el.classList.remove("hidden");
}

async function api(path, options = {}) {
    const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
    if (state.token) {
        headers.Authorization = "Bearer " + state.token;
    }
    let response;
    try {
        response = await fetch(path, { ...options, headers });
    } catch (err) {
        throw new Error("No se pudo conectar con el servidor. Inténtelo de nuevo.");
    }
    let body = null;
    const text = await response.text();
    if (text) {
        try {
            body = JSON.parse(text);
        } catch (e) {
            body = text;
        }
    }
    if (!response.ok) {
        throw new Error(body && body.error ? body.error : "Error de la solicitud (" + response.status + ").");
    }
    return body;
}

function togglePasswordVisibility() {
    const show = els.password.type === "password";
    els.password.type = show ? "text" : "password";
    els.togglePassword.setAttribute("aria-label", show ? "Ocultar contraseña" : "Mostrar contraseña");
    els.togglePassword.querySelector(".icon-eye").style.display = show ? "none" : "";
    els.togglePassword.querySelector(".icon-eye-off").style.display = show ? "" : "none";
}

async function login(event) {
    event.preventDefault();
    els.loginError.classList.add("hidden");
    els.loginSubmit.disabled = true;
    els.loginSubmit.classList.add("loading");
    try {
        const result = await api("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({ username: els.username.value, password: els.password.value }),
        });
        state.token = result.token;
        state.role = result.role;
        localStorage.setItem("sig.token", state.token);
        localStorage.setItem("sig.role", state.role);
        els.username.value = "";
        els.password.value = "";
        enterApp();
    } catch (err) {
        showMessage(els.loginError, err.message, "error");
        els.loginError.classList.remove("hidden");
    } finally {
        els.loginSubmit.disabled = false;
        els.loginSubmit.classList.remove("loading");
    }
}

function logout() {
    localStorage.removeItem("sig.token");
    localStorage.removeItem("sig.role");
    state.token = null;
    state.role = null;
    show("login");
}

function switchTab(name) {
    const search = name === "search";
    els.searchTab.classList.toggle("hidden", !search);
    els.registerTab.classList.toggle("hidden", search);
    els.tabSearch.classList.toggle("active", search);
    els.tabRegister.classList.toggle("active", !search);
}

function enterApp() {
    const username = state.role === "ADMINISTRATOR" ? "admin" : "consulta";
    els.currentUser.textContent = username;
    els.userAvatar.textContent = username.charAt(0).toUpperCase();
    els.currentRole.textContent = state.role === "ADMINISTRATOR" ? "Administrador" : "Consulta";
    els.tabRegister.classList.toggle("hidden", state.role !== "ADMINISTRATOR");
    if (state.role !== "ADMINISTRATOR") {
        els.registerTab.classList.add("hidden");
    }
    switchTab("search");
    show("app");
    initMap();
    loadCategories();
    loadEntities();
}

function initMap() {
    if (state.map) {
        return;
    }
    state.map = L.map("map", { maxZoom: 19 }).setView([4.6, -74.07], 12);
    state.entityLayer = L.layerGroup().addTo(state.map);
    state.map.on("click", onMapClick);

    state.osmLayer = L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
        maxZoom: 19,
        attribution:
            '&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noopener">OpenStreetMap</a> contribuidores',
    });

    state.localLayer = L.geoJSON(null, {
        style: { color: "#94a3b8", weight: 1, fillColor: "#e8edf2", fillOpacity: 0.6 },
    });

    state.baseControl = L.control
        .layers(
            {
                "OpenStreetMap": state.osmLayer,
                "Mapa local": state.localLayer,
            },
            null,
            { position: "topleft" }
        )
        .addTo(state.map);

    state.osmLayer.addTo(state.map);
    state.activeBasemap = "osm";
    setupOsmFallback(state.osmLayer);
    setupCoordinatesControl();
    state.map.on("baselayerchange", onBasemapUserChange);
    loadLocalBasemap();
}

async function loadLocalBasemap() {
    try {
        const geojson = await api("/api/map/basemap");
        state.localLayer.addData(geojson);
        if (state.activeBasemap === "local") {
            const bounds = state.localLayer.getBounds();
            if (bounds.isValid()) {
                state.map.fitBounds(bounds);
            }
        }
    } catch (err) {
        showMessage(els.listStatus, "No se pudo cargar el mapa base local.", "error");
    }
}

function setupOsmFallback(layer) {
    layer.on("tileerror", () => {
        if (state.activeBasemap !== "osm") {
            return;
        }
        state.osmConsecutiveErrors += 1;
        if (state.osmConsecutiveErrors >= 3) {
            activateFallback();
        }
    });
    layer.on("tileload", () => {
        if (state.activeBasemap !== "osm") {
            return;
        }
        state.osmConsecutiveErrors = 0;
    });
}

function activateFallback() {
    if (state.fallbackActive) {
        return;
    }
    state.fallbackActive = true;
    switchBaseMap("local");
    showBasemapNotice("No fue posible cargar OpenStreetMap. Se activó el mapa local.");
}

function switchBaseMap(name) {
    if (name === "local" && state.activeBasemap !== "local") {
        state.activeBasemap = "local";
        if (state.map.hasLayer(state.osmLayer)) {
            state.map.removeLayer(state.osmLayer);
        }
        if (!state.map.hasLayer(state.localLayer)) {
            state.localLayer.addTo(state.map);
        }
    } else if (name === "osm" && state.activeBasemap !== "osm") {
        state.activeBasemap = "osm";
        if (state.map.hasLayer(state.localLayer)) {
            state.map.removeLayer(state.localLayer);
        }
        state.osmLayer.addTo(state.map);
        state.osmConsecutiveErrors = 0;
        state.fallbackActive = false;
        hideBasemapNotice();
    }
}

function onBasemapUserChange(event) {
    if (event.layer === state.osmLayer) {
        state.activeBasemap = "osm";
        state.osmConsecutiveErrors = 0;
        state.fallbackActive = false;
        hideBasemapNotice();
    } else {
        state.activeBasemap = "local";
    }
}

function setupCoordinatesControl() {
    const CoordinatesControl = L.Control.extend({
        options: { position: "bottomleft" },
        onAdd: function () {
            const div = L.DomUtil.create("div", "map-coordinates");
            div.textContent = "Latitud: -- | Longitud: --";
            this._div = div;
            return div;
        },
    });
    state.coordinatesControl = new CoordinatesControl().addTo(state.map);
    state.map.on("mousemove", (event) => {
        state.coordinatesControl._div.textContent =
            "Latitud: " + event.latlng.lat.toFixed(6) + " | Longitud: " + event.latlng.lng.toFixed(6);
    });
    state.map.on("mouseout", () => {
        state.coordinatesControl._div.textContent = "Latitud: -- | Longitud: --";
    });
}

function showBasemapNotice(text) {
    els.basemapNotice.textContent = text;
    els.basemapNotice.classList.remove("hidden");
    clearTimeout(state.basemapNoticeTimer);
    state.basemapNoticeTimer = setTimeout(() => {
        els.basemapNotice.classList.add("hidden");
    }, 6000);
}

function hideBasemapNotice() {
    clearTimeout(state.basemapNoticeTimer);
    els.basemapNotice.classList.add("hidden");
}

async function loadCategories() {
    try {
        const result = await api("/api/categories");
        state.categories = result.categories;
        fillCategorySelect(els.qCategory, true);
        fillCategorySelect(els.eCategory, false);
    } catch (err) {
        showMessage(els.listStatus, err.message, "error");
    }
}

function fillCategorySelect(select, includeAll) {
    select.innerHTML = "";
    if (includeAll) {
        const all = document.createElement("option");
        all.value = "";
        all.textContent = "Todas";
        select.appendChild(all);
    }
    for (const category of state.categories) {
        const option = document.createElement("option");
        option.value = category.id;
        option.textContent = category.name;
        select.appendChild(option);
    }
}

async function loadEntities() {
    try {
        const result = await api("/api/entities");
        state.entities = result.entities;
        renderEntities();
    } catch (err) {
        showMessage(els.listStatus, err.message, "error");
    }
}

function renderEntities() {
    renderList(state.entities);
    renderMap(state.entities);
}

function renderList(entities) {
    els.entityList.innerHTML = "";
    if (entities.length === 0) {
        els.listStatus.classList.add("hidden");
        els.resultsCount.classList.add("hidden");
        els.emptyState.classList.remove("hidden");
        return;
    }
    els.listStatus.classList.add("hidden");
    els.emptyState.classList.add("hidden");
    els.resultsCount.textContent = entities.length;
    els.resultsCount.classList.remove("hidden");
    for (const entity of entities) {
        const li = document.createElement("li");
        li.dataset.id = entity.id;
        li.addEventListener("click", () => selectEntity(entity.id));

        const icon = document.createElement("span");
        icon.className = "entity-type-icon";
        icon.innerHTML = natureIcon(entity.nature);

        const body = document.createElement("div");
        body.className = "entity-body";
        const name = document.createElement("div");
        name.className = "entity-name";
        name.textContent = entity.name;
        const meta = document.createElement("div");
        meta.className = "entity-meta";
        meta.textContent = natureLabel(entity.nature) + " · " + categoryLabel(entity.category);
        body.appendChild(name);
        body.appendChild(meta);

        li.appendChild(icon);
        li.appendChild(body);

        if (state.role === "ADMINISTRATOR") {
            const actions = document.createElement("div");
            actions.className = "entity-actions";
            const edit = document.createElement("button");
            edit.type = "button";
            edit.className = "entity-action edit";
            edit.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z"/></svg> Editar';
            edit.addEventListener("click", () => startEdit(entity));
            const remove = document.createElement("button");
            remove.type = "button";
            remove.className = "entity-action remove";
            remove.title = "Eliminar";
            remove.setAttribute("aria-label", "Eliminar " + entity.name);
            remove.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M3 6h18"/><path d="M8 6V4a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v2"/><path d="m19 6-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/></svg>';
            remove.addEventListener("click", () => removeEntity(entity));
            actions.appendChild(edit);
            actions.appendChild(remove);
            body.appendChild(actions);
        }
        els.entityList.appendChild(li);
    }
}

function popupContent(entity) {
    const attributes = Object.entries(entity.attributes || {})
        .map(([key, value]) => key + ": " + value)
        .join("<br>");
    return (
        "<div class='popup-title'>" + escapeHtml(entity.name) + "</div>" +
        "<div class='popup-meta'>" + escapeHtml(natureLabel(entity.nature)) + " · " + escapeHtml(categoryLabel(entity.category)) + "</div>" +
        (entity.description ? "<div class='popup-detail'>" + escapeHtml(entity.description) + "</div>" : "") +
        (attributes ? "<div class='popup-detail'>" + attributes + "</div>" : "")
    );
}

function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}

function renderMap(entities) {
    deselectSelection();
    state.entityLayer.clearLayers();
    state.layersById = {};
    for (const entity of entities) {
        const geometry = entity.geometry;
        let layer = null;
        if (geometry.type === "Point") {
            const [lon, lat] = geometry.coordinates;
            layer = L.marker([lat, lon], { icon: entityMarkerIcon() })
                .bindTooltip(escapeHtml(entity.name), { direction: "top" })
                .bindPopup(popupContent(entity));
        } else if (geometry.type === "LineString") {
            const latLngs = geometry.coordinates.map(([lon, lat]) => [lat, lon]);
            layer = L.polyline(latLngs, { color: "#1f6feb", weight: 3 });
            layer._originalStyle = { color: "#1f6feb", weight: 3, fillColor: null, fillOpacity: null };
            layer.bindTooltip(escapeHtml(entity.name), { sticky: true });
            layer.bindPopup(popupContent(entity));
        } else if (geometry.type === "Polygon") {
            const ring = geometry.coordinates[0].map(([lon, lat]) => [lat, lon]);
            layer = L.polygon(ring, { color: "#c2410c", weight: 2, fillOpacity: 0.2 });
            layer._originalStyle = { color: "#c2410c", weight: 2, fillColor: null, fillOpacity: 0.2 };
            layer.bindTooltip(escapeHtml(entity.name), { sticky: true });
            layer.bindPopup(popupContent(entity));
        }
        layer._entityId = entity.id;
        layer.addTo(state.entityLayer);
        state.layersById[entity.id] = layer;
    }
    if (entities.length === 0) {
        els.mapInfo.classList.add("hidden");
    } else {
        els.mapInfo.textContent = entities.length + " entidad" + (entities.length === 1 ? "" : "es") + " visible" + (entities.length === 1 ? "" : "s");
        els.mapInfo.classList.remove("hidden");
    }
    restoreDrawState();
}

function restoreDrawState() {
    if (state.geometry) {
        renderGeometryPreview(state.geometry);
    } else if (state.drawVertices.length > 0) {
        refreshDrawPreview(els.eType.value);
    }
}

function selectEntity(id) {
    deselectSelection();
    state.selectedId = id;
    const li = document.querySelector('.entity-list li[data-id="' + id + '"]');
    if (li) {
        li.classList.add("selected");
    }
    const layer = state.layersById[id];
    if (!layer) {
        return;
    }
    if (typeof layer.setLatLng === "function") {
        const latLng = layer.getLatLng();
        layer._highlightMarker = L.circleMarker(latLng, highlightMarkerStyle).addTo(state.entityLayer);
        state.map.panTo(latLng);
    } else {
        layer.setStyle({
            color: HIGHLIGHT_COLOR,
            weight: (layer.options.weight || 2) + 2,
            fillColor: HIGHLIGHT_COLOR,
            fillOpacity: 0.4,
        });
        layer.bringToFront();
        state.map.flyTo(layer.getBounds().getCenter(), Math.max(state.map.getZoom(), 13));
    }
}

function deselectSelection() {
    if (!state.selectedId) {
        return;
    }
    const layer = state.layersById[state.selectedId];
    if (layer) {
        if (layer._highlightMarker) {
            state.entityLayer.removeLayer(layer._highlightMarker);
            layer._highlightMarker = null;
        } else if (typeof layer.setStyle === "function" && layer._originalStyle) {
            layer.setStyle(layer._originalStyle);
        }
    }
    const previous = document.querySelector(".entity-list li.selected");
    if (previous) {
        previous.classList.remove("selected");
    }
    state.selectedId = null;
}

async function runQuery(event) {
    event.preventDefault();
    const params = new URLSearchParams();
    if (els.qCategory.value) params.set("category", els.qCategory.value);
    if (els.qText.value.trim()) params.set("text", els.qText.value.trim());
    if (els.qAttribute.value.trim()) params.set("attribute", els.qAttribute.value.trim());
    if (els.qLat.value || els.qLon.value || els.qRadius.value) {
        params.set("lat", els.qLat.value);
        params.set("lon", els.qLon.value);
        params.set("radiusKm", els.qRadius.value);
    }
    try {
        const result = await api("/api/entities/query?" + params.toString());
        renderList(result.entities);
        renderMap(result.entities);
    } catch (err) {
        showMessage(els.listStatus, err.message, "error");
    }
}

function clearQuery() {
    els.qCategory.value = "";
    els.qText.value = "";
    els.qAttribute.value = "";
    els.qLat.value = "";
    els.qLon.value = "";
    els.qRadius.value = "";
    loadEntities();
}

async function removeEntity(entity) {
    if (!confirm("¿Desea eliminar la entidad '" + entity.name + "'?")) {
        return;
    }
    try {
        await api("/api/entities/" + encodeURIComponent(entity.id), { method: "DELETE" });
        await loadEntities();
    } catch (err) {
        showMessage(els.listStatus, err.message, "error");
    }
}

function startEdit(entity) {
    els.formTitle.textContent = "Editar entidad";
    els.entityId.value = entity.id;
    els.eName.value = entity.name;
    els.eDescription.value = entity.description || "";
    els.eNature.value = entity.nature;
    els.eCategory.value = entity.category;
    els.eAttributes.value = Object.entries(entity.attributes || {})
        .map(([key, value]) => key + ": " + value)
        .join("\n");
    els.eType.value = entity.geometry.type;
    state.prevGeometryType = entity.geometry.type;
    state.geometry = entity.geometry;
    state.drawVertices = [];
    state.drawingLocked = entity.geometry.type !== "Point";
    renderGeometryPreview(entity.geometry);
    updateGeometryStatus();
    els.formCancel.classList.remove("hidden");
    switchTab("register");
}

function resetForm() {
    els.formTitle.textContent = "Registrar entidad";
    els.entityForm.reset();
    els.entityId.value = "";
    state.editingId = null;
    state.geometry = null;
    state.drawVertices = [];
    state.drawingLocked = false;
    state.prevGeometryType = "Point";
    clearDrawPreview();
    hideMessage(els.formStatus);
    updateGeometryStatus();
    els.formCancel.classList.add("hidden");
}

function hideMessage(el) {
    el.classList.add("hidden");
    el.textContent = "";
}

async function saveEntity(event) {
    event.preventDefault();
    const id = els.entityId.value;
    const geometry = state.geometry;
    if (!geometry) {
        showMessage(els.formStatus, "Debe definir la geometría haciendo clic en el mapa.", "error");
        return;
    }
    const body = {
        name: els.eName.value,
        description: els.eDescription.value,
        nature: els.eNature.value,
        category: els.eCategory.value,
        attributes: parseAttributes(els.eAttributes.value),
        geometry: geometry,
    };
    try {
        if (id) {
            await api("/api/entities/" + encodeURIComponent(id), { method: "PUT", body: JSON.stringify(body) });
        } else {
            await api("/api/entities", { method: "POST", body: JSON.stringify(body) });
        }
        resetForm();
        switchTab("search");
        await loadEntities();
    } catch (err) {
        showMessage(els.formStatus, err.message, "error");
    }
}

function parseAttributes(text) {
    const attributes = {};
    for (const line of text.split("\n")) {
        const trimmed = line.trim();
        if (!trimmed) {
            continue;
        }
        const separator = trimmed.indexOf(":");
        if (separator === -1) {
            attributes[trimmed] = "";
        } else {
            attributes[trimmed.slice(0, separator).trim()] = trimmed.slice(separator + 1).trim();
        }
    }
    return attributes;
}

function isDrawModeActive() {
    return (
        state.role === "ADMINISTRATOR" &&
        !els.registerTab.classList.contains("hidden") &&
        !state.drawingLocked
    );
}

function onMapClick(event) {
    if (!isDrawModeActive()) {
        return;
    }
    const type = els.eType.value;
    const latLng = event.latlng;
    if (type === "Point") {
        state.geometry = {
            type: "Point",
            coordinates: [round(latLng.lng), round(latLng.lat)],
        };
        if (state.drawMarker) {
            state.entityLayer.removeLayer(state.drawMarker);
            state.drawMarker = null;
        }
        state.drawMarker = L.circleMarker([latLng.lat, latLng.lng], {
            radius: 8,
            color: HIGHLIGHT_COLOR,
            weight: 3,
            fillColor: HIGHLIGHT_COLOR,
            fillOpacity: 0.5,
        }).addTo(state.entityLayer);
        updateGeometryStatus();
        return;
    }
    state.drawVertices.push([round(latLng.lng), round(latLng.lat)]);
    refreshDrawPreview(type);
    updateGeometryStatus();
}

function countDistinctVertices(vertices) {
    const seen = new Set();
    for (const [lon, lat] of vertices) {
        seen.add(lon + "," + lat);
    }
    return seen.size;
}

function renderGeometryPreview(geometry) {
    clearDrawPreview();
    if (!geometry) {
        return;
    }
    if (geometry.type === "Point") {
        const [lon, lat] = geometry.coordinates;
        state.drawMarker = L.circleMarker([lat, lon], {
            radius: 8,
            color: HIGHLIGHT_COLOR,
            weight: 3,
            fillColor: HIGHLIGHT_COLOR,
            fillOpacity: 0.5,
        }).addTo(state.entityLayer);
    } else if (geometry.type === "LineString") {
        const latLngs = geometry.coordinates.map(([lon, lat]) => [lat, lon]);
        state.drawPreview = L.polyline(latLngs, { color: HIGHLIGHT_COLOR, weight: 3 }).addTo(state.entityLayer);
    } else if (geometry.type === "Polygon") {
        const ring = geometry.coordinates[0].map(([lon, lat]) => [lat, lon]);
        state.drawPreview = L.polygon(ring, { color: HIGHLIGHT_COLOR, fillOpacity: 0.2 }).addTo(state.entityLayer);
    }
}

function refreshDrawPreview(type) {
    if (state.drawPreview) {
        state.entityLayer.removeLayer(state.drawPreview);
    }
    if (state.drawVertices.length === 0) {
        state.drawPreview = null;
        return;
    }
    const latLngs = state.drawVertices.map(([lon, lat]) => [lat, lon]);
    if (type === "LineString" && latLngs.length >= 2) {
        state.drawPreview = L.polyline(latLngs, { color: "#ff9800", weight: 3 }).addTo(state.entityLayer);
    } else if (type === "Polygon" && latLngs.length >= 2) {
        const closed = latLngs.slice();
        if (closed[0][0] !== closed[closed.length - 1][0]) {
            closed.push(closed[0]);
        }
        state.drawPreview = L.polygon(closed, { color: "#ff9800", fillOpacity: 0.2 }).addTo(state.entityLayer);
    }
}

function finishDraw() {
    const type = els.eType.value;
    if (type === "LineString") {
        if (state.drawVertices.length < 2) {
            showMessage(els.formStatus, "Una línea requiere al menos dos puntos.", "error");
            return;
        }
        state.geometry = { type: "LineString", coordinates: state.drawVertices.slice() };
    } else if (type === "Polygon") {
        if (countDistinctVertices(state.drawVertices) < 3) {
            showMessage(els.formStatus, "Un polígono requiere al menos tres puntos distintos.", "error");
            return;
        }
        const ring = state.drawVertices.slice();
        const first = ring[0];
        const last = ring[ring.length - 1];
        if (first[0] !== last[0] || first[1] !== last[1]) {
            ring.push([first[0], first[1]]);
        }
        state.geometry = { type: "Polygon", coordinates: [ring] };
    }
    state.drawVertices = [];
    state.drawingLocked = true;
    renderGeometryPreview(state.geometry);
    updateGeometryStatus();
}

function clearGeometry() {
    state.geometry = null;
    state.drawVertices = [];
    state.drawingLocked = false;
    clearDrawPreview();
    updateGeometryStatus();
}

function clearDrawPreview() {
    if (state.drawMarker) {
        state.entityLayer.removeLayer(state.drawMarker);
        state.drawMarker = null;
    }
    if (state.drawPreview) {
        state.entityLayer.removeLayer(state.drawPreview);
        state.drawPreview = null;
    }
}

function updateGeometryStatus() {
    const type = els.eType.value;
    const label = GEOMETRY_LABELS[type] || type;
    if (state.geometry) {
        const count = type === "Point" ? 1 : type === "Polygon" ? state.geometry.coordinates[0].length : state.geometry.coordinates.length;
        els.geometryStatus.textContent = "Geometría definida (" + label + ", " + count + " coordenadas).";
        if (type !== "Point") {
            els.geometryStatus.textContent += " Usa 'Borrar geometría' para redefinirla.";
        }
        els.geometryFinish.classList.add("hidden");
    } else if (state.drawVertices.length > 0) {
        els.geometryStatus.textContent = state.drawVertices.length + " punto(s) añadido(s). Haz clic para agregar más.";
        els.geometryFinish.classList.remove("hidden");
    } else {
        els.geometryStatus.textContent = "Sin geometría definida.";
        els.geometryFinish.classList.add("hidden");
    }
}

function round(value) {
    return Math.round(value * 100000) / 100000;
}

els.loginForm.addEventListener("submit", login);
els.togglePassword.addEventListener("click", togglePasswordVisibility);
els.logoutBtn.addEventListener("click", logout);
els.tabSearch.addEventListener("click", () => switchTab("search"));
els.tabRegister.addEventListener("click", () => switchTab("register"));
els.queryForm.addEventListener("submit", runQuery);
els.queryClear.addEventListener("click", clearQuery);
els.entityForm.addEventListener("submit", saveEntity);
els.formCancel.addEventListener("click", cancelEdit);
els.geometryClear.addEventListener("click", clearGeometry);
els.geometryFinish.addEventListener("click", finishDraw);
els.eType.addEventListener("change", onGeometryTypeChange);

function cancelEdit() {
    resetForm();
    switchTab("search");
}

function onGeometryTypeChange(event) {
    const hasGeometry = state.geometry !== null || state.drawVertices.length > 0;
    if (hasGeometry && state.prevGeometryType !== els.eType.value) {
        if (!confirm("Cambiar el tipo de geometría borrará la geometría actual. ¿Desea continuar?")) {
            els.eType.value = state.prevGeometryType;
            return;
        }
    }
    state.prevGeometryType = els.eType.value;
    clearGeometry();
}

(function restoreSession() {
    const token = localStorage.getItem("sig.token");
    const role = localStorage.getItem("sig.role");
    if (token && role) {
        state.token = token;
        state.role = role;
        enterApp();
    } else {
        show("login");
    }
})();
