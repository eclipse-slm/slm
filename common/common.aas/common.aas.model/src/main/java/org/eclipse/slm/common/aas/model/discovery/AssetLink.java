package org.eclipse.slm.common.aas.model.discovery;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AssetLink   {
  @JsonProperty("name")

  private String name = null;

  @JsonProperty("value")

  private String value = null;


  public AssetLink name(String name) { 

    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   **/

  @Schema(required = true, description = "")
  
  @NotNull
@Pattern(regexp="^([\\x09\\x0a\\x0d\\x20-\\ud7ff\\ue000-\\ufffd]|\\ud800[\\udc00-\\udfff]|[\\ud801-\\udbfe][\\udc00-\\udfff]|\\udbff[\\udc00-\\udfff])*$") @Size(min=1,max=64)   public String getName() {
    return name;
  }



  public void setName(String name) { 

    this.name = name;
  }

  public AssetLink value(String value) { 

    this.value = value;
    return this;
  }

  /**
   * Get value
   * @return value
   **/

  @Schema(required = true, description = "")
  
  @NotNull
@Pattern(regexp="^([\\x09\\x0a\\x0d\\x20-\\ud7ff\\ue000-\\ufffd]|\\ud800[\\udc00-\\udfff]|[\\ud801-\\udbfe][\\udc00-\\udfff]|\\udbff[\\udc00-\\udfff])*$") @Size(min=1,max=2048)   public String getValue() {  
    return value;
  }



  public void setValue(String value) { 

    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AssetLink assetLink = (AssetLink) o;
    return Objects.equals(this.name, assetLink.name) &&
        Objects.equals(this.value, assetLink.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AssetLink {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
