using UnityEngine;

public class RemotePlayer : MonoBehaviour
{
    public Vector3 velocity;

    private void Start()
    {
        velocity = Vector3.zero;
    }

    // Update is called once per frame
    private void Update()
    {
        //scoot the player based on their velocity. we will use the velocity from Minecraft instead of unity to be accurate
        transform.Translate(velocity * Time.deltaTime);
    }
}