function apiUrl() {
  return document.getElementById('apiBaseInput').value.replace(/\/$/, '');
}

function mostrarAlerta(msg, tipo = 'info') {
  const box = document.getElementById('alertaApi');
  box.className = `alert alert-${tipo} py-2 small`;
  box.textContent = msg;
}

async function req(path, method = 'GET', body = null) {
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(apiUrl() + path, opts);
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || `Error HTTP ${res.status}`);
  return data;
}

// 1. PELICULAS
async function cargarPeliculas() {
  const lista = await req('/peliculas');
  const tb = document.getElementById('tbodyPeliculas');
  tb.innerHTML = lista.map(p => `
    <tr>
      <td>${p.id_pelicula}</td>
      <td>${p.titulo}</td>
      <td>${p.duracion_minutos} min</td>
      <td>${p.clasificacion}</td>
      <td>${p.genero || ''}</td>
      <td>
        <button class="btn btn-xs btn-outline-info py-0 px-2" onclick='editarPelicula(${JSON.stringify(p)})'>Editar</button>
        <button class="btn btn-xs btn-outline-danger py-0 px-2" onclick="eliminarRegistro('/peliculas/${p.id_pelicula}', cargarPeliculas)">Eliminar</button>
      </td>
    </tr>`).join('');
}

function editarPelicula(p) {
  document.getElementById('pelId').value = p.id_pelicula;
  document.getElementById('pelTitulo').value = p.titulo;
  document.getElementById('pelDuracion').value = p.duracion_minutos;
  document.getElementById('pelClasif').value = p.clasificacion;
  document.getElementById('pelGenero').value = p.genero || '';
}

function limpiarPelicula() {
  document.getElementById('formPelicula').reset();
  document.getElementById('pelId').value = '';
}

document.getElementById('formPelicula').addEventListener('submit', async e => {
  e.preventDefault();
  const id = document.getElementById('pelId').value;
  const payload = {
    titulo: document.getElementById('pelTitulo').value,
    duracion_minutos: Number(document.getElementById('pelDuracion').value),
    clasificacion: document.getElementById('pelClasif').value,
    genero: document.getElementById('pelGenero').value,
    activo: true
  };
  try {
    await req(id ? `/peliculas/${id}` : '/peliculas', id ? 'PUT' : 'POST', payload);
    mostrarAlerta('Película guardada en MySQL.', 'success');
    limpiarPelicula();
    cargarPeliculas();
  } catch (err) { mostrarAlerta(err.message, 'danger'); }
});

// 2. SALAS
async function cargarSalas() {
  const lista = await req('/salas');
  const tb = document.getElementById('tbodySalas');
  tb.innerHTML = lista.map(s => `
    <tr>
      <td>${s.id_sala}</td>
      <td>${s.nombre_sucursal || s.id_sucursal}</td>
      <td>${s.nombre_sala}</td>
      <td>${s.capacidad}</td>
      <td>${s.tipo_sala}</td>
      <td>
        <button class="btn btn-xs btn-outline-info py-0 px-2" onclick='editarSala(${JSON.stringify(s)})'>Editar</button>
        <button class="btn btn-xs btn-outline-danger py-0 px-2" onclick="eliminarRegistro('/salas/${s.id_sala}', cargarSalas)">Eliminar</button>
      </td>
    </tr>`).join('');
}

function editarSala(s) {
  document.getElementById('salaId').value = s.id_sala;
  document.getElementById('salaNombre').value = s.nombre_sala;
  document.getElementById('salaCapacidad').value = s.capacidad;
  document.getElementById('salaTipo').value = s.tipo_sala;
}

function limpiarSala() {
  document.getElementById('formSala').reset();
  document.getElementById('salaId').value = '';
}

document.getElementById('formSala').addEventListener('submit', async e => {
  e.preventDefault();
  const id = document.getElementById('salaId').value;
  const payload = {
    id_sucursal: 1,
    nombre_sala: document.getElementById('salaNombre').value,
    capacidad: Number(document.getElementById('salaCapacidad').value),
    tipo_sala: document.getElementById('salaTipo').value,
    activo: true
  };
  try {
    await req(id ? `/salas/${id}` : '/salas', id ? 'PUT' : 'POST', payload);
    mostrarAlerta('Sala guardada en MySQL.', 'success');
    limpiarSala();
    cargarSalas();
  } catch (err) { mostrarAlerta(err.message, 'danger'); }
});

// 3. FUNCIONES
async function cargarFunciones() {
  const lista = await req('/funciones');
  const tb = document.getElementById('tbodyFunciones');
  tb.innerHTML = lista.map(f => `
    <tr>
      <td>${f.id_funcion}</td>
      <td>${f.pelicula}</td>
      <td>${f.sala}</td>
      <td>${f.fecha_inicio}</td>
      <td>${f.estado}</td>
      <td>
        <button class="btn btn-xs btn-outline-info py-0 px-2" onclick='editarFuncion(${JSON.stringify(f)})'>Editar</button>
        <button class="btn btn-xs btn-outline-danger py-0 px-2" onclick="eliminarRegistro('/funciones/${f.id_funcion}', cargarFunciones)">Eliminar</button>
      </td>
    </tr>`).join('');
}

function editarFuncion(f) {
  document.getElementById('funId').value = f.id_funcion;
  document.getElementById('funCartelera').value = f.id_cartelera;
  document.getElementById('funPelicula').value = f.id_pelicula;
  document.getElementById('funSala').value = f.id_sala;
  document.getElementById('funFecha').value = f.fecha_inicio;
}

function limpiarFuncion() {
  document.getElementById('formFuncion').reset();
  document.getElementById('funId').value = '';
}

document.getElementById('formFuncion').addEventListener('submit', async e => {
  e.preventDefault();
  const id = document.getElementById('funId').value;
  const payload = {
    id_cartelera: Number(document.getElementById('funCartelera').value),
    id_pelicula: Number(document.getElementById('funPelicula').value),
    id_sala: Number(document.getElementById('funSala').value),
    fecha_inicio: document.getElementById('funFecha').value,
    estado: 'PROGRAMADA'
  };
  try {
    await req(id ? `/funciones/${id}` : '/funciones', id ? 'PUT' : 'POST', payload);
    mostrarAlerta('Función guardada en MySQL.', 'success');
    limpiarFuncion();
    cargarFunciones();
  } catch (err) { mostrarAlerta(err.message, 'danger'); }
});

// 4. PROMOCIONES
async function cargarPromociones() {
  const lista = await req('/promociones');
  const tb = document.getElementById('tbodyPromociones');
  tb.innerHTML = lista.map(pr => `
    <tr>
      <td>${pr.id_promocion}</td>
      <td>${pr.nombre_promocion}</td>
      <td>${pr.tipo_descuento}</td>
      <td>${pr.valor_descuento}</td>
      <td>${String(pr.fecha_inicio).slice(0,10)} a ${String(pr.fecha_fin).slice(0,10)}</td>
      <td>
        <button class="btn btn-xs btn-outline-info py-0 px-2" onclick='editarPromocion(${JSON.stringify(pr)})'>Editar</button>
        <button class="btn btn-xs btn-outline-danger py-0 px-2" onclick="eliminarRegistro('/promociones/${pr.id_promocion}', cargarPromociones)">Eliminar</button>
      </td>
    </tr>`).join('');
}

function editarPromocion(pr) {
  document.getElementById('promoId').value = pr.id_promocion;
  document.getElementById('promoNombre').value = pr.nombre_promocion;
  document.getElementById('promoTipo').value = pr.tipo_descuento;
  document.getElementById('promoValor').value = pr.valor_descuento;
  document.getElementById('promoInicio').value = String(pr.fecha_inicio).slice(0,10);
  document.getElementById('promoFin').value = String(pr.fecha_fin).slice(0,10);
}

document.getElementById('formPromocion').addEventListener('submit', async e => {
  e.preventDefault();
  const id = document.getElementById('promoId').value;
  const payload = {
    nombre_promocion: document.getElementById('promoNombre').value,
    tipo_descuento: document.getElementById('promoTipo').value,
    valor_descuento: Number(document.getElementById('promoValor').value),
    fecha_inicio: document.getElementById('promoInicio').value,
    fecha_fin: document.getElementById('promoFin').value,
    activo: true
  };
  try {
    await req(id ? `/promociones/${id}` : '/promociones', id ? 'PUT' : 'POST', payload);
    mostrarAlerta('Promoción guardada en MySQL.', 'success');
    document.getElementById('formPromocion').reset();
    document.getElementById('promoId').value = '';
    cargarPromociones();
  } catch (err) { mostrarAlerta(err.message, 'danger'); }
});

// 5. PRODUCTOS
async function cargarProductos() {
  const lista = await req('/productos');
  const tb = document.getElementById('tbodyProductos');
  tb.innerHTML = lista.map(p => `
    <tr>
      <td>${p.id_producto}</td>
      <td>${p.nombre_producto}</td>
      <td>${p.categoria || ''}</td>
      <td>$${Number(p.precio).toFixed(2)}</td>
      <td>${p.stock}</td>
      <td>
        <button class="btn btn-xs btn-outline-info py-0 px-2" onclick='editarProducto(${JSON.stringify(p)})'>Editar</button>
        <button class="btn btn-xs btn-outline-danger py-0 px-2" onclick="eliminarRegistro('/productos/${p.id_producto}', cargarProductos)">Eliminar</button>
      </td>
    </tr>`).join('');
}

function editarProducto(p) {
  document.getElementById('prodId').value = p.id_producto;
  document.getElementById('prodNombre').value = p.nombre_producto;
  document.getElementById('prodCategoria').value = p.categoria || 'BOTANAS';
  document.getElementById('prodPrecio').value = p.precio;
  document.getElementById('prodStock').value = p.stock;
}

function limpiarProducto() {
  document.getElementById('formProducto').reset();
  document.getElementById('prodId').value = '';
}

document.getElementById('formProducto').addEventListener('submit', async e => {
  e.preventDefault();
  const id = document.getElementById('prodId').value;
  const payload = {
    nombre_producto: document.getElementById('prodNombre').value,
    categoria: document.getElementById('prodCategoria').value,
    precio: Number(document.getElementById('prodPrecio').value),
    stock: Number(document.getElementById('prodStock').value),
    activo: true
  };
  try {
    await req(id ? `/productos/${id}` : '/productos', id ? 'PUT' : 'POST', payload);
    mostrarAlerta('Producto guardado en MySQL.', 'success');
    limpiarProducto();
    cargarProductos();
  } catch (err) { mostrarAlerta(err.message, 'danger'); }
});

// VENTAS EN VIVO
async function cargarVentas() {
  const lista = await req('/ventas');
  const tb = document.getElementById('tbodyVentas');
  tb.innerHTML = lista.map(v => `
    <tr>
      <td>#${v.id_venta}</td>
      <td>${v.id_sucursal}</td>
      <td><span class="badge bg-${v.tipo_venta === 'TAQUILLA' ? 'primary' : 'success'}">${v.tipo_venta}</span></td>
      <td>${v.fecha_venta}</td>
      <td>$${Number(v.total).toFixed(2)}</td>
      <td>${v.estado}</td>
    </tr>`).join('');
}

async function eliminarRegistro(endpoint, recargarFn) {
  if (!confirm('¿Confirmas eliminar este registro de MySQL?')) return;
  try {
    const res = await req(endpoint, 'DELETE');
    mostrarAlerta(res.mensaje || 'Registro eliminado.', 'warning');
    recargarFn();
  } catch (err) {
    mostrarAlerta(err.message, 'danger');
  }
}

async function recargarTodo() {
  try {
    await Promise.all([cargarPeliculas(), cargarSalas(), cargarFunciones(), cargarPromociones(), cargarProductos(), cargarVentas()]);
    mostrarAlerta('Conectado a Spring Boot + MySQL (aperture_entrega). Catálogos sincronizados.', 'success');
  } catch (err) {
    mostrarAlerta('No se pudo conectar a ' + apiUrl() + ' (' + err.message + ')', 'danger');
  }
}

window.addEventListener('DOMContentLoaded', recargarTodo);
