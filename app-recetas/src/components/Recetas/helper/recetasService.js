import { recetasApi } from "./recetasApi";

const listarRecetas = async () => {
    const response = await recetasApi.get(`/`);
    console.log('listarRecetas::response::%O', response);

    return response.data;
}

export {
    listarRecetas
}