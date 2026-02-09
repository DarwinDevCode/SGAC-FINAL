import http from "../config/http-common";
import type { Convocatoria } from "../types/Convocatoria";
import type { AxiosResponse } from "axios";

class ConvocatoriaService {
  private endpoint: string = "/convocatorias";

  getAll(): Promise<AxiosResponse<Convocatoria[]>> {
    return http.get<Convocatoria[]>(this.endpoint);
  }

  getById(id: number): Promise<AxiosResponse<Convocatoria>> {
    return http.get<Convocatoria>(`${this.endpoint}/${id}`);
  }

  create(data: Convocatoria): Promise<AxiosResponse<Convocatoria>> {
    return http.post<Convocatoria>(`${this.endpoint}/crear`, data);
  }

  update(data: Convocatoria): Promise<AxiosResponse<Convocatoria>> {
    return http.put<Convocatoria>(`${this.endpoint}/actualizar`, data);
  }

  delete(id: number): Promise<AxiosResponse<void>> {
    return http.delete<void>(`${this.endpoint}/${id}`);
  }
}

export default new ConvocatoriaService();
