from fastapi import APIRouter, Depends, HTTPException, status

from app.utils import validate_token
from app.schemas import ProfileDataSchema

router = APIRouter()

@router.get("/{profile_id}", response_model=ProfileDataSchema)
async def get_profile_data(profile_id: str, profile_data: ProfileDataSchema = Depends(validate_token)):
    if profile_id != profile_data.profile_id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied")

    return profile_data
