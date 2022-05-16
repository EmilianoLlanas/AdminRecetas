import { recetasApi } from "./recetasApi";

const listarRecetas = async () => {
    const response = await recetasApi.get(`/`);
    console.log('listarRecetas::response::%O', response);

    return response.data;
}

const actualizarReceta = async (id, data) => {
    const response = await recetasApi.put(`/${id}`, data);
    console.log('actualizarReceta::response::%O', response);

    return response.data;
}

export {
    listarRecetas,
    actualizarReceta
}