package iranga.mg.social.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FileResponse {
    public String fileDownloadUri;
    public String fileThumbnailUri;
}
